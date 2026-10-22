package pe.odontopoint.dto;

/** Filtros HTTP del módulo de ingresos. Las fechas visibles utilizan dd/MM/aaaa. */
public class ConsultaIngresos {
  private String tipo = "MES";
  private Integer anio;
  private Integer mes;
  private Integer trimestre;
  private String desde;
  private String hasta;
  private String q = "";
  private int pagina = 0;
  private int tamano = 20;

  public String getTipo() { return tipo; }
  public void setTipo(String tipo) { this.tipo = tipo; }
  public Integer getAnio() { return anio; }
  public void setAnio(Integer anio) { this.anio = anio; }
  public Integer getMes() { return mes; }
  public void setMes(Integer mes) { this.mes = mes; }
  public Integer getTrimestre() { return trimestre; }
  public void setTrimestre(Integer trimestre) { this.trimestre = trimestre; }
  public String getDesde() { return desde; }
  public void setDesde(String desde) { this.desde = desde; }
  public String getHasta() { return hasta; }
  public void setHasta(String hasta) { this.hasta = hasta; }
  public String getQ() { return q; }
  public void setQ(String q) { this.q = q; }
  public int getPagina() { return pagina; }
  public void setPagina(int pagina) { this.pagina = pagina; }
  public int getTamano() { return tamano; }
  public void setTamano(int tamano) { this.tamano = tamano; }
}
