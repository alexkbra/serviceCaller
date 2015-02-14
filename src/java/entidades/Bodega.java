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
@Table(name = "bodega")
@NamedQueries({
    @NamedQuery(name = "Bodega.findAll", query = "SELECT b FROM Bodega b")})
public class Bodega implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 45)
    @Column(name = "cdbodega")
    private String cdbodega;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 45)
    @Column(name = "nombrebodega")
    private String nombrebodega;
    @Size(max = 45)
    @Column(name = "dsbodega")
    private String dsbodega;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "bodega", fetch = FetchType.LAZY)
    private List<Ubicacion> ubicacionList;
    @JoinColumn(name = "usuario_identificacion", referencedColumnName = "identificacion")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Usuario usuario;

    public Bodega() {
    }

    public Bodega(Integer id) {
        this.id = id;
    }

    public Bodega(Integer id, String cdbodega, String nombrebodega) {
        this.id = id;
        this.cdbodega = cdbodega;
        this.nombrebodega = nombrebodega;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCdbodega() {
        return cdbodega;
    }

    public void setCdbodega(String cdbodega) {
        this.cdbodega = cdbodega;
    }

    public String getNombrebodega() {
        return nombrebodega;
    }

    public void setNombrebodega(String nombrebodega) {
        this.nombrebodega = nombrebodega;
    }

    public String getDsbodega() {
        return dsbodega;
    }

    public void setDsbodega(String dsbodega) {
        this.dsbodega = dsbodega;
    }

    public List<Ubicacion> getUbicacionList() {
        return ubicacionList;
    }

    public void setUbicacionList(List<Ubicacion> ubicacionList) {
        this.ubicacionList = ubicacionList;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
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
        if (!(object instanceof Bodega)) {
            return false;
        }
        Bodega other = (Bodega) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entidades.Bodega[ id=" + id + " ]";
    }
    
}
