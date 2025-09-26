package com.sistrans.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.sistrans.entity.Disponibilidad;

@Repository
public class DisponibilidadRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public boolean verificarSuperposicion(Long cedulaConductor, String nuevaFranjaHoraria) {
        String sql = "SELECT COUNT(*) FROM DISPONIBILIDAD d " +
                    "JOIN VEHICULO v ON d.tipo_transporte = v.tipo " +
                    "WHERE v.cedula_dueño = ? " +
                    "AND d.dia = TRUNC(SYSDATE) " +
                    "AND (SUBSTR(?, 1, 5) < SUBSTR(d.franja_horaria, 7, 5) " +
                    "AND SUBSTR(?, 7, 5) > SUBSTR(d.franja_horaria, 1, 5))";
        
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, cedulaConductor, nuevaFranjaHoraria, nuevaFranjaHoraria);
        return count != null && count > 0;
    }

    public Long insertarDisponibilidad(Disponibilidad disponibilidad) {
        String sql = "INSERT INTO DISPONIBILIDAD (dia, franja_horaria, tipo_transporte) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, disponibilidad.getDia(), disponibilidad.getFranjaHoraria(), disponibilidad.getTipoTransporte());
        
        String getIdSql = "SELECT id_disponibilidad FROM DISPONIBILIDAD WHERE dia = ? AND franja_horaria = ? AND tipo_transporte = ? ORDER BY id_disponibilidad DESC FETCH FIRST 1 ROWS ONLY";
        return jdbcTemplate.queryForObject(getIdSql, Long.class, disponibilidad.getDia(), disponibilidad.getFranjaHoraria(), disponibilidad.getTipoTransporte());
    }

    public void actualizarDisponibilidad(Disponibilidad disponibilidad) {
        String sql = "UPDATE DISPONIBILIDAD SET dia = ?, franja_horaria = ?, tipo_transporte = ? WHERE id_disponibilidad = ?";
        jdbcTemplate.update(sql, disponibilidad.getDia(), disponibilidad.getFranjaHoraria(), 
                          disponibilidad.getTipoTransporte(), disponibilidad.getIdDisponibilidad());
    }
}
