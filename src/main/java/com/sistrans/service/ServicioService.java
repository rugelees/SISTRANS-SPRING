package com.sistrans.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sistrans.entity.SolicitudServicio;
import com.sistrans.entity.Servicio;
import com.sistrans.entity.ConductorDisponible;
import com.sistrans.repository.ServicioRepository;
import com.sistrans.exception.AppLogicException;

@Service
public class ServicioService {

    @Autowired
    private ServicioRepository servicioRepository;

    @Transactional
    public Servicio crearServicio(SolicitudServicio solicitud) {
        ConductorDisponible conductor = servicioRepository.buscarConductorDisponible(solicitud.getTipoServicio());
        if (conductor == null) {
            throw new AppLogicException("No hay conductores disponibles para este tipo de servicio.");
        }

        double costoTotal = calcularCostoEstimado();

        Servicio servicio = new Servicio();
        servicio.setCedulaSolicitante(solicitud.getCedulaSolicitante());
        servicio.setCedulaConductor(conductor.getCedula());
        servicio.setPlacaVehiculo(conductor.getPlaca());
        servicio.setIdPuntoPartida(solicitud.getIdPuntoPartida());
        servicio.setTarjetaCredito(solicitud.getNumeroTarjeta());
        servicio.setTipo(solicitud.getTipoServicio());
        servicio.setCostoTotal(costoTotal);

        Long idNuevoServicio = servicioRepository.insertarServicio(servicio);

        // Insertar destinos
        for (Long idDestino : solicitud.getIdsPuntosDestino()) {
            servicioRepository.insertarDestino(idNuevoServicio, idDestino);
        }

        // Insertar en tabla de especialización según el tipo de servicio
        switch (solicitud.getTipoServicio().toUpperCase()) {
            case "TRANSPORTE_PASAJEROS":
                if (solicitud.getNivelTransporte() == null) {
                    throw new AppLogicException("El nivel de transporte es requerido para servicios de transporte de pasajeros.");
                }
                servicioRepository.insertarTransportePasajeros(idNuevoServicio, solicitud.getNivelTransporte());
                break;
                
            case "ENTREGA_COMIDA":
                if (solicitud.getNombreRestaurante() == null || solicitud.getNombreRestaurante().trim().isEmpty()) {
                    throw new AppLogicException("El nombre del restaurante es requerido para servicios de entrega de comida.");
                }
                servicioRepository.insertarEntregaComida(idNuevoServicio, solicitud.getNombreRestaurante());
                break;
                
            case "TRANSPORTE_MERCANCIA":
                if (solicitud.getPesoCarga() == null || solicitud.getPesoCarga() <= 0) {
                    throw new AppLogicException("El peso de la carga es requerido y debe ser mayor a 0 para servicios de transporte de mercancía.");
                }
                servicioRepository.insertarTransporteMercancia(idNuevoServicio, solicitud.getPesoCarga());
                break;
                
            default:
                throw new AppLogicException("Tipo de servicio no válido: " + solicitud.getTipoServicio());
        }

        servicio.setId(idNuevoServicio);
        return servicio;
    }

    private double calcularCostoEstimado() {
        return 20000.0;
    }

    public void finalizarServicio(Long idServicio, String distancia, Double costoTotal) {
        servicioRepository.finalizarServicio(idServicio, distancia, costoTotal);
    }
}
