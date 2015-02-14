/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package entidades;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author andrea
 */
@Entity
@Table(name = "ubicacion")
@NamedQueries({
    @NamedQuery(name = "Ubicacion.findAll", query = "SELECT u FROM Ubicacion u")})
public class Ubicacion implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 45)
    @Column(name = "cdubicacion")
    private String cdubicacion;
    @Size(max = 45)
    @Column(name = "dsubicacion")
    private String dsubicacion;
    @Size(max = 45)
    @Column(name = "col")
    private String col;
    @Size(max = 45)
    @Column(name = "fil")
    private String fil;
    @JoinColumn(name = "bodega_id", referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Bodega bodega;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "ubicacion", fetch = FetchType.LAZY)
    private List<Producto> productoList;

    public Ubicacion() {
    }

    public Ubicacion(Integer id) {
        this.id = id;
    }

    public Ubicacion(Integer id, String cdubicacion) {
        this.id = id;
        this.cdubicacion = cdubicacion;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCdubicacion() {
        return cdubicacion;
    }

    public void setCdubicacion(String cdubicacion) {
        this.cdubicacion = cdubicacion;
    }

    public String getDsubicacion() {
        return dsubicacion;
    }

    public void setDsubicacion(String dsubicacion) {
        this.dsubicacion = dsubicacion;
    }

    public String getCol() {
        return col;
    }

    public void setCol(String col) {
        this.col = col;
    }

    public String getFil() {
        return fil;
    }

    public void setFil(String fil) {
        this.fil = fil;
    }

    public Bodega getBodega() {
        return bodega;
    }

    public void setBodega(Bodega bodega) {
        this.bodega = bodega;
    }

    public List<Producto> getProductoList() {
        return productoList;
    }

    public void setProductoList(List<Producto> productoList) {
        this.productoList = productoList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Ubicacion)) {
            return false;
        }
        Ubicacion other = (Ubicacion) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entidades.Ubicacion[ id=" + id + " ]";
    }
    
}
