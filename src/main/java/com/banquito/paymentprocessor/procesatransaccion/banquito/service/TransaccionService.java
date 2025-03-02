package com.banquito.paymentprocessor.procesatransaccion.banquito.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import com.banquito.paymentprocessor.procesatransaccion.banquito.client.BancoClient;
import com.banquito.paymentprocessor.procesatransaccion.banquito.client.FraudeClient;
import com.banquito.paymentprocessor.procesatransaccion.banquito.client.MarcaClient;
import com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto.ProcesoBancarioRequest;
import com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto.ProcesoBancarioResponse;
import com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto.ValidacionFraudeRequest;
import com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto.ValidacionFraudeResponse;
import com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto.ValidacionMarcaRequest;
import com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto.ValidacionMarcaResponse;
import com.banquito.paymentprocessor.procesatransaccion.banquito.model.Transaccion;
import com.banquito.paymentprocessor.procesatransaccion.banquito.model.HistorialEstadoTransaccion;
import com.banquito.paymentprocessor.procesatransaccion.banquito.repository.TransaccionRepository;
import com.banquito.paymentprocessor.procesatransaccion.banquito.repository.HistorialEstadoTransaccionRepository;
import com.banquito.paymentprocessor.procesatransaccion.banquito.exception.NotFoundException;
import com.banquito.paymentprocessor.procesatransaccion.banquito.exception.TransaccionRechazadaException;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransaccionService {

    private final TransaccionRepository transaccionRepository;
    private final HistorialEstadoTransaccionRepository historialRepository;
    private final MarcaClient marcaClient;
    private final FraudeClient fraudeClient;
    private final BancoClient bancoClient;
    private final RedisService redisService;
    
    // Estados de transacción
    public static final String ESTADO_PENDIENTE = "PEN";
    public static final String ESTADO_VALIDACION_MARCA = "VMA";
    public static final String ESTADO_VALIDACION_FRAUDE = "VFR";
    public static final String ESTADO_PROCESO_DEBITO = "DEB";
    public static final String ESTADO_PROCESO_CREDITO = "CRE";
    public static final String ESTADO_COMPLETADA = "COM";
    public static final String ESTADO_RECHAZADA = "REC";
    public static final String ESTADO_ERROR = "ERR";
    public static final String ESTADO_FRAUDE = "FRA";

    public Transaccion procesarTransaccion(Transaccion transaccion) {
        log.info("Iniciando procesamiento de transacción: {}", transaccion.getCodigoUnico());
        
        try {
            // Paso 1: Inicializar y guardar la transacción
            inicializarTransaccion(transaccion);
            
            // Paso 2: Validar fraude
            try {
                validarFraude(transaccion);
            } catch (TransaccionRechazadaException e) {
                log.warn("Transacción rechazada en validación de fraude: {}", e.getMessage());
                throw e;
            } catch (Exception e) {
                log.error("Error en validación de fraude: {}", e.getMessage(), e);
                actualizarEstadoTransaccion(transaccion, ESTADO_ERROR, 
                        "Error en validación de fraude: " + e.getMessage());
                throw new TransaccionRechazadaException("Error en validación de fraude: " + e.getMessage());
            }
            
            // Paso 3: Validar tarjeta con marca
            try {
                validarMarca(transaccion);
            } catch (TransaccionRechazadaException e) {
                log.warn("Transacción rechazada en validación de marca: {}", e.getMessage());
                throw e;
            } catch (Exception e) {
                log.error("Error en validación con marca: {}", e.getMessage(), e);
                actualizarEstadoTransaccion(transaccion, ESTADO_ERROR, 
                        "Error en validación con marca: " + e.getMessage());
                throw new TransaccionRechazadaException("Error en validación con marca: " + e.getMessage());
            }
            
            // Paso 4: Procesar débito a la tarjeta
            try {
                procesarDebitoTarjeta(transaccion);
            } catch (TransaccionRechazadaException e) {
                log.warn("Transacción rechazada en proceso de débito: {}", e.getMessage());
                throw e;
            } catch (Exception e) {
                log.error("Error en proceso de débito: {}", e.getMessage(), e);
                actualizarEstadoTransaccion(transaccion, ESTADO_ERROR, 
                        "Error en proceso de débito: " + e.getMessage());
                throw new TransaccionRechazadaException("Error en proceso de débito: " + e.getMessage());
            }
            
            // Paso 5: Procesar crédito al comercio
            try {
                procesarCreditoComercio(transaccion);
            } catch (TransaccionRechazadaException e) {
                log.warn("Transacción rechazada en proceso de crédito: {}", e.getMessage());
                throw e;
            } catch (Exception e) {
                log.error("Error en proceso de crédito: {}", e.getMessage(), e);
                reversoDebitoTarjeta(transaccion);
                actualizarEstadoTransaccion(transaccion, ESTADO_ERROR, 
                        "Error en proceso de crédito: " + e.getMessage());
                throw new TransaccionRechazadaException("Error en proceso de crédito: " + e.getMessage());
            }
            
            // Transacción completada exitosamente
            actualizarEstadoTransaccion(transaccion, ESTADO_COMPLETADA, 
                    "Transacción procesada exitosamente");
            
            log.info("Transacción procesada exitosamente: {}", transaccion.getCodTransaccion());
            return transaccion;
            
        } catch (TransaccionRechazadaException e) {
            // Las excepciones de rechazo ya tienen el estado actualizado
            log.warn("Transacción rechazada: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado procesando transacción: {}", e.getMessage(), e);
            try {
                actualizarEstadoTransaccion(transaccion, ESTADO_ERROR, 
                        "Error inesperado: " + e.getMessage());
            } catch (Exception ex) {
                log.error("No se pudo actualizar estado de error en transacción: {}", ex.getMessage());
                // Si falló la actualización del estado, intentamos eliminar la transacción de Redis directamente
                try {
                    if (transaccion.getId() != null) {
                        redisService.deleteTransaccion(transaccion.getId());
                    }
                    if (transaccion.getCodTransaccion() != null) {
                        redisService.deleteTransaccionByCodigo(transaccion.getCodTransaccion());
                    }
                    log.info("Transacción eliminada de Redis por error crítico");
                } catch (Exception deleteEx) {
                    log.error("No se pudo eliminar la transacción de Redis: {}", deleteEx.getMessage());
                }
            }
            throw new RuntimeException("Error procesando transacción: " + e.getMessage(), e);
        }
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void inicializarTransaccion(Transaccion transaccion) {
        log.debug("Inicializando transacción");
        
        try {
            transaccion.setFechaTransaccion(LocalDateTime.now());
            transaccion.setEstado(ESTADO_PENDIENTE);
            
            if (transaccion.getCodTransaccion() == null || transaccion.getCodTransaccion().isEmpty()) {
                transaccion.setCodTransaccion(UUID.randomUUID().toString().substring(0, 10));
            }
            
            // Guardado en la base de datos primero
            try {
                Transaccion transaccionGuardada = transaccionRepository.save(transaccion);
                transaccion.setId(transaccionGuardada.getId());
                log.info("Transacción guardada en PostgreSQL con ID: {}", transaccion.getId());
                
                // SOLO si se guarda exitosamente en PostgreSQL, intentamos guardar en Redis
                try {
                    redisService.saveTransaccion(transaccion);
                    log.info("Transacción guardada en Redis con código: {}", transaccion.getCodTransaccion());
                } catch (Exception ex) {
                    log.error("Error al guardar transacción en Redis (continuando proceso): {}", ex.getMessage(), ex);
                }
                
                // SOLO si se guarda exitosamente en PostgreSQL, registramos el evento inicial
                try {
                    registrarHistorialEstado(transaccion, ESTADO_PENDIENTE, "Transacción recibida del gateway");
                    log.debug("Historial de estado inicial registrado");
                } catch (Exception ex) {
                    log.error("Error al registrar historial de estado inicial: {}", ex.getMessage(), ex);
                }
                
                log.info("Transacción inicializada con código: {}", transaccion.getCodTransaccion());
            } catch (Exception ex) {
                log.error("Error crítico al guardar transacción en PostgreSQL: {}", ex.getMessage(), ex);
                throw new RuntimeException("Error al guardar transacción en base de datos: no se guardará en Redis ni en historial", ex);
            }
        } catch (Exception e) {
            log.error("Error en inicialización de transacción: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    private void validarFraude(Transaccion transaccion) {
        log.debug("Validando fraude para transacción: {}", transaccion.getCodTransaccion());
        actualizarEstadoTransaccion(transaccion, ESTADO_VALIDACION_FRAUDE, 
                "Iniciando validación de fraude");
        
        ValidacionFraudeRequest fraudeRequest = new ValidacionFraudeRequest();
        fraudeRequest.setNumeroTarjeta(transaccion.getNumeroTarjeta());
        fraudeRequest.setMonto(transaccion.getMonto());
        fraudeRequest.setCodTransaccion(transaccion.getCodTransaccion());
        fraudeRequest.setSwiftBanco(transaccion.getSwiftBancoTarjeta());
        
        ValidacionFraudeResponse fraudeResponse = fraudeClient.validarTransaccion(fraudeRequest);
        
        // Asignar transaccionValida basado en la negación de esFraude si es que transaccionValida es nulo
        if (fraudeResponse.getTransaccionValida() == null) {
            fraudeResponse.setTransaccionValida(!fraudeResponse.isEsFraude());
        }
        
        if (!fraudeResponse.getTransaccionValida()) {
            log.warn("Transacción identificada como posible fraude: {}", fraudeResponse.getMensaje());
            actualizarEstadoTransaccion(transaccion, ESTADO_FRAUDE, fraudeResponse.getMensaje());
            throw new TransaccionRechazadaException("Posible fraude detectado: " + fraudeResponse.getMensaje());
        }
        
        log.info("Validación de fraude exitosa para transacción: {}", transaccion.getCodTransaccion());
    }
    
    private void validarMarca(Transaccion transaccion) {
        log.debug("Validando tarjeta con la marca para transacción: {}", transaccion.getCodTransaccion());
        actualizarEstadoTransaccion(transaccion, ESTADO_VALIDACION_MARCA, 
                "Iniciando validación con marca de tarjeta");
        
        String marcaTarjeta = obtenerMarcaTarjeta(transaccion.getNumeroTarjeta());
        log.info("Marca de tarjeta identificada: {}", marcaTarjeta);
        
        ValidacionMarcaRequest marcaRequest = new ValidacionMarcaRequest();
        marcaRequest.setNumeroTarjeta(transaccion.getNumeroTarjeta());
        marcaRequest.setFechaCaducidad(transaccion.getFechaCaducidad());
        marcaRequest.setCvv(transaccion.getCvv());
        marcaRequest.setMonto(transaccion.getMonto());
        marcaRequest.setCodigoTransaccion(transaccion.getCodTransaccion());
        marcaRequest.setMarca(marcaTarjeta);
        
        ValidacionMarcaResponse marcaResponse = marcaClient.validarTarjeta(marcaRequest);
        
        if (!marcaResponse.getTarjetaValida()) {
            log.warn("Tarjeta inválida o rechazada: {}", marcaResponse.getMensaje());
            actualizarEstadoTransaccion(transaccion, ESTADO_RECHAZADA, marcaResponse.getMensaje());
            throw new TransaccionRechazadaException("Tarjeta rechazada: " + marcaResponse.getMensaje());
        }
        
        transaccion.setSwiftBancoTarjeta(marcaResponse.getSwiftBanco());
        log.info("Validación de marca exitosa para transacción: {}", transaccion.getCodTransaccion());
    }
    
    private void procesarDebitoTarjeta(Transaccion transaccion) {
        log.debug("Procesando débito a tarjeta para transacción: {}", transaccion.getCodTransaccion());
        actualizarEstadoTransaccion(transaccion, ESTADO_PROCESO_DEBITO, 
                "Iniciando proceso de débito a tarjeta");
        
        ProcesoBancarioRequest request = new ProcesoBancarioRequest();
        request.setNumeroTarjeta(transaccion.getNumeroTarjeta());
        request.setMonto(transaccion.getMonto());
        request.setCodigoTransaccion(transaccion.getCodTransaccion());
        request.setSwiftBanco(transaccion.getSwiftBancoTarjeta());
        request.setReferencia("DEBITO-" + transaccion.getCodTransaccion());
        
        ProcesoBancarioResponse response = bancoClient.procesarTransaccion(request);
        
        if (!response.getTransaccionExitosa()) {
            log.warn("Débito a tarjeta rechazado: {}", response.getMensaje());
            actualizarEstadoTransaccion(transaccion, ESTADO_RECHAZADA, 
                    "Débito rechazado: " + response.getMensaje());
            throw new TransaccionRechazadaException("Débito a tarjeta rechazado: " + response.getMensaje());
        }
        
        log.info("Débito a tarjeta exitoso para transacción: {}", transaccion.getCodTransaccion());
    }
    
    private void procesarCreditoComercio(Transaccion transaccion) {
        log.debug("Procesando crédito a comercio para transacción: {}", transaccion.getCodTransaccion());
        actualizarEstadoTransaccion(transaccion, ESTADO_PROCESO_CREDITO, 
                "Iniciando proceso de crédito a comercio");
        
        ProcesoBancarioRequest request = new ProcesoBancarioRequest();
        request.setNumeroTarjeta(transaccion.getNumeroTarjeta());
        request.setMonto(transaccion.getMonto());
        request.setCodigoTransaccion(transaccion.getCodTransaccion());
        request.setSwiftBanco(transaccion.getSwiftBancoTarjeta());
        request.setReferencia("CREDITO-" + transaccion.getCodTransaccion());
        
        ProcesoBancarioResponse response = bancoClient.procesarTransaccion(request);
        
        if (!response.getTransaccionExitosa()) {
            log.error("Crédito a comercio fallido pero débito ya realizado: {}", response.getMensaje());
            reversoDebitoTarjeta(transaccion);
            actualizarEstadoTransaccion(transaccion, ESTADO_RECHAZADA, 
                    "Crédito rechazado: " + response.getMensaje());
            throw new TransaccionRechazadaException("Crédito a comercio rechazado: " + response.getMensaje());
        }
        
        log.info("Crédito a comercio exitoso para transacción: {}", transaccion.getCodTransaccion());
    }
    
    private void reversoDebitoTarjeta(Transaccion transaccion) {
        log.warn("Iniciando reverso de débito para transacción: {}", transaccion.getCodTransaccion());
        
        try {
            ProcesoBancarioRequest request = new ProcesoBancarioRequest();
            request.setNumeroTarjeta(transaccion.getNumeroTarjeta());
            request.setMonto(transaccion.getMonto());
            request.setCodigoTransaccion("REV-" + transaccion.getCodTransaccion());
            request.setSwiftBanco(transaccion.getSwiftBancoTarjeta());
            request.setReferencia("REVERSO-" + transaccion.getCodTransaccion());
            
            ProcesoBancarioResponse response = bancoClient.procesarTransaccion(request);
            
            if (response.getTransaccionExitosa()) {
                log.info("Reverso de débito exitoso para transacción: {}", transaccion.getCodTransaccion());
            } else {
                log.error("Reverso de débito fallido para transacción: {}", transaccion.getCodTransaccion());
            }
        } catch (Exception e) {
            log.error("Error en reverso de débito: {}", e.getMessage(), e);
        }
    }

    public Transaccion obtenerTransaccionPorCodigo(String codTransaccion) {
        log.info("Buscando transacción con código: {}", codTransaccion);
        
        Transaccion transaccionRedis = redisService.getTransaccionByCodigo(codTransaccion);
        if (transaccionRedis != null) {
            log.info("Transacción encontrada en Redis: {}", transaccionRedis.getCodTransaccion());
            return transaccionRedis;
        }
        
        return transaccionRepository.findByCodTransaccion(codTransaccion)
                .orElseThrow(() -> new NotFoundException("Transacción no encontrada con código: " + codTransaccion));
    }

    public List<Transaccion> buscarTransaccionesPorFecha(LocalDateTime desde, LocalDateTime hasta) {
        log.info("Buscando transacciones entre {} y {}", desde, hasta);
        return transaccionRepository.findByFechaTransaccionBetweenOrderByFechaTransaccionDesc(desde, hasta);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void actualizarEstadoTransaccion(Transaccion transaccion, String estado, String mensaje) {
        // Asegurarse de que la transacción tenga un ID antes de guardar
        if (transaccion.getId() == null) {
            log.error("No se puede actualizar una transacción sin ID");
            throw new RuntimeException("Intento de actualizar transacción sin ID");
        }
        
        try {
            // Actualizar en PostgreSQL primero
            transaccion.setEstado(estado);
            Transaccion transaccionActualizada = transaccionRepository.save(transaccion);
            log.info("Transacción con ID {} actualizada en PostgreSQL con estado: {}", transaccion.getId(), estado);
            
            // Registrar historial sólo si la actualización en PostgreSQL fue exitosa
            try {
                registrarHistorialEstado(transaccion, estado, mensaje);
            } catch (Exception e) {
                log.error("Error al registrar historial de estado: {}", e.getMessage(), e);
                // Continuamos el proceso aunque falle el registro del historial
            }
            
            // Si es un estado de error o rechazo, ELIMINAR la transacción de Redis
            if (ESTADO_ERROR.equals(estado) || ESTADO_RECHAZADA.equals(estado) || ESTADO_FRAUDE.equals(estado)) {
                try {
                    redisService.deleteTransaccion(transaccion.getId());
                    redisService.deleteTransaccionByCodigo(transaccion.getCodTransaccion());
                    log.info("Transacción con ID {} y código {} eliminada de Redis por estado: {}", 
                             transaccion.getId(), transaccion.getCodTransaccion(), estado);
                } catch (Exception e) {
                    log.error("Error al eliminar transacción de Redis: {}", e.getMessage(), e);
                }
            } else {
                // Actualizar en Redis sólo si NO es estado de error y si la actualización en PostgreSQL fue exitosa
                try {
                    redisService.updateTransaccion(transaccion);
                    log.info("Transacción actualizada en Redis: {}", transaccion.getCodTransaccion());
                } catch (Exception e) {
                    log.error("Error al actualizar transacción en Redis: {}", e.getMessage(), e);
                }
            }
            
            log.info("Estado de transacción actualizado a: {} - {}", estado, mensaje);
        } catch (Exception e) {
            log.error("Error al actualizar estado de transacción en PostgreSQL: {}", e.getMessage(), e);
            throw new RuntimeException("Error al actualizar transacción en base de datos", e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void registrarHistorialEstado(Transaccion transaccion, String estado, String mensaje) {
        // Validación de entradas
        if (transaccion == null || transaccion.getCodTransaccion() == null) {
            log.error("No se puede registrar historial sin una transacción o código válido");
            return;
        }
        
        // Truncar mensaje si es necesario (máximo 200 caracteres)
        String mensajeTruncado = null;
        if (mensaje != null) {
            if (mensaje.length() > 200) {
                mensajeTruncado = mensaje.substring(0, 197) + "...";
                log.debug("Mensaje truncado por exceder longitud máxima: {}", mensajeTruncado);
            } else {
                mensajeTruncado = mensaje;
            }
        } else {
            mensajeTruncado = "Sin información adicional";
        }
        
        try {
            HistorialEstadoTransaccion historial = new HistorialEstadoTransaccion();
            historial.setCodHistorialEstado(UUID.randomUUID().toString().substring(0, 10));
            historial.setCodTransaccion(transaccion.getCodTransaccion());
            historial.setEstado(estado);
            historial.setMensaje(mensajeTruncado);
            historial.setFechaEstadoCambio(LocalDateTime.now());
            historialRepository.save(historial);
            log.debug("Historial de estado registrado: {} - {}", estado, mensajeTruncado);
        } catch (Exception e) {
            log.error("Error al registrar historial de estado: {} - {}", estado, e.getMessage(), e);
        }
    }

    private String obtenerMarcaTarjeta(String numeroTarjeta) {
        if (numeroTarjeta.startsWith("4")) return "VISA";
        if (numeroTarjeta.startsWith("5")) return "MASTERCARD";
        if (numeroTarjeta.startsWith("34") || numeroTarjeta.startsWith("37")) return "AMEX";
        return "DESCONOCIDA";
    }

    public List<Transaccion> findAll() {
        return transaccionRepository.findAll();
    }

    public List<Transaccion> findByNumeroTarjeta(String numeroTarjeta) {
        return transaccionRepository.findByNumeroTarjeta(numeroTarjeta);
    }

    public List<Transaccion> findByEstado(String estado) {
        return transaccionRepository.findByEstado(estado);
    }
} 