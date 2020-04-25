package com.karam.transport;

public class Carga {
    private Long numcar;
    String dtsaida, dtfinal;

    public Carga(Long numcar, String dtsaida, String dtfinal) {
        this.numcar = numcar;
        this.dtsaida = dtsaida;
        this.dtfinal = dtfinal;
    }

    public Carga() {

    }

    public Long getNumcar() {
        return numcar;
    }

    public void setNumcar(Long numcar) {
        this.numcar = numcar;
    }


    public String getDtsaida() {
        return dtsaida;
    }

    public void setDtsaida(String dtsaida) {
        this.dtsaida = dtsaida;
    }

    public String getDtfinal() {
        return dtfinal;
    }

    public void setDtfinal(String dtfinal) {
        this.dtfinal = dtfinal;
    }
}
