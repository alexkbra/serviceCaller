/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package entidades;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author andrea
 */
@Entity
@Table(name = "producto")
@NamedQueries({
    @NamedQuery(name = "Producto.findAll", query = "SELECT p FROM Producto p")})
public class Producto implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "idP")
    private Integer idP;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 45)
    @Column(name = "nombreproducto")
    private String nombreproducto;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 45)
    @Column(name = "cdproducto")
    private String cdproducto;
    @Size(max = 45)
    @Column(name = "dsproducto")
    private String dsproducto;
    @Size(max = 45)
    @Column(name = "dencidad")
    private String dencidad;
    @Size(max = 45)
    @Column(name = "peso")
    private String peso;
    @Size(max = 45)
    @Column(name = "idproductosquimicos")
    private String idproductosquimicos;
    @JoinColumn(name = "tipo_producto_id", referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Tipoproducto tipoproducto;
    @JoinColumn(name = "ubicacion_id", referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Ubicacion ubicacion;

    public Producto() {
    }

    public Producto(Integer idP) {
        this.idP = idP;
    }

    public Producto(Integer idP, String nombreproducto, String cdproducto) {
        this.idP = idP;
        this.nombreproducto = nombreproducto;
        this.cdproducto = cdproducto;
    }

    public Integer getIdP() {
        return idP;
    }

    public void setIdP(Integer idP) {
        this.idP = idP;
    }

    public String getNombreproducto() {
        return nombreproducto;
    }

    public void setNombreproducto(String nombreproducto) {
        this.nombreproducto = nombreproducto;
    }

    public String getCdproducto() {
        return cdproducto;
    }

    public void setCdproducto(String cdproducto) {
        this.cdproducto = cdproducto;
    }

    public String getDsproducto() {
        return dsproducto;
    }

    public void setDsproducto(String dsproducto) {
        this.dsproducto = dsproducto;
    }

    public String getDencidad() {
        return dencidad;
    }

    public void setDencidad(String dencidad) {
        this.dencidad = dencidad;
    }

    public String getPeso() {
        return peso;
    }

    public void setPeso(String peso) {
        this.peso = peso;
    }

    public String getIdproductosquimicos() {
        return idproductosquimicos;
    }

    public void setIdproductosquimicos(String idproductosquimicos) {
        this.idproductosquimicos = idproductosquimicos;
    }

    public Tipoproducto getTipoproducto() {
        return tipoproducto;
    }

    public void setTipoproducto(Tipoproducto tipoproducto) {
        this.tipoproducto = tipoproducto;
    }

    public Ubicacion getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(Ubicacion ubicacion) {
        this.ubicacion = ubicacion;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idP != null ? idP.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Producto)) {
            return false;
        }
        Producto other = (Producto) object;
        if ((this.idP == null && other.idP != null) || (this.idP != null && !this.idP.equals(other.idP))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entidades.Producto[ idP=" + idP + " ]";
    }
    
}
