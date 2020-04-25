package com.karam.transport;

public class Prod {
    Long codprod,numnota,qt,qtfalta,codbarra1,codbarra2;
    Integer codmotivodev,stdev;
    String descricao;
    public Prod(){}


    public Prod(Long codprod, Long numnota, Long qt,Long qtfalta, Long codbarra1, Long codbarra2, Integer codmotivodev, Integer stdev, String descricao) {
        this.codprod = codprod;
        this.numnota = numnota;
        this.qt = qt;
        this.codbarra1 = codbarra1;
        this.codbarra2 = codbarra2;
        this.codmotivodev = codmotivodev;
        this.stdev = stdev;
        this.descricao = descricao;
        this.qtfalta = qtfalta;
    }

    public Long getQtfalta() {
        return qtfalta;
    }

    public void setQtfalta(Long qtfalta) {
        this.qtfalta = qtfalta;
    }

    public Long getCodprod() {
        return codprod;
    }

    public void setCodprod(Long codprod) {
        this.codprod = codprod;
    }

    public Long getNumnota() {
        return numnota;
    }

    public void setNumnota(Long numnota) {
        this.numnota = numnota;
    }

    public Long getQt() {
        return qt;
    }

    public void setQt(Long qt) {
        this.qt = qt;
    }

    public Long getCodbarra1() {
        return codbarra1;
    }

    public void setCodbarra1(Long codbarra1) {
        this.codbarra1 = codbarra1;
    }

    public Long getCodbarra2() {
        return codbarra2;
    }

    public void setCodbarra2(Long codbarra2) {
        this.codbarra2 = codbarra2;
    }

    public Integer getCodmotivodev() {
        return codmotivodev;
    }

    public void setCodmotivodev(Integer codmotivodev) {
        this.codmotivodev = codmotivodev;
    }

    public Integer getStdev() {
        return stdev;
    }

    public void setStdev(Integer stdev) {
        this.stdev = stdev;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}

