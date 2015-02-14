/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package controladores;

import controladores.exceptions.NonexistentEntityException;
import controladores.exceptions.RollbackFailureException;
import entidades.Producto;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import entidades.Tipoproducto;
import entidades.Ubicacion;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;

/**
 *
 * @author andrea
 */
public class ProductoJpaController implements Serializable {

    public ProductoJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Producto producto) throws RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Tipoproducto tipoproducto = producto.getTipoproducto();
            if (tipoproducto != null) {
                tipoproducto = em.getReference(tipoproducto.getClass(), tipoproducto.getId());
                producto.setTipoproducto(tipoproducto);
            }
            Ubicacion ubicacion = producto.getUbicacion();
            if (ubicacion != null) {
                ubicacion = em.getReference(ubicacion.getClass(), ubicacion.getId());
                producto.setUbicacion(ubicacion);
            }
            em.persist(producto);
            if (tipoproducto != null) {
                tipoproducto.getProductoList().add(producto);
                tipoproducto = em.merge(tipoproducto);
            }
            if (ubicacion != null) {
                ubicacion.getProductoList().add(producto);
                ubicacion = em.merge(ubicacion);
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Producto producto) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Producto persistentProducto = em.find(Producto.class, producto.getIdP());
            Tipoproducto tipoproductoOld = persistentProducto.getTipoproducto();
            Tipoproducto tipoproductoNew = producto.getTipoproducto();
            Ubicacion ubicacionOld = persistentProducto.getUbicacion();
            Ubicacion ubicacionNew = producto.getUbicacion();
            if (tipoproductoNew != null) {
                tipoproductoNew = em.getReference(tipoproductoNew.getClass(), tipoproductoNew.getId());
                producto.setTipoproducto(tipoproductoNew);
            }
            if (ubicacionNew != null) {
                ubicacionNew = em.getReference(ubicacionNew.getClass(), ubicacionNew.getId());
                producto.setUbicacion(ubicacionNew);
            }
            producto = em.merge(producto);
            if (tipoproductoOld != null && !tipoproductoOld.equals(tipoproductoNew)) {
                tipoproductoOld.getProductoList().remove(producto);
                tipoproductoOld = em.merge(tipoproductoOld);
            }
            if (tipoproductoNew != null && !tipoproductoNew.equals(tipoproductoOld)) {
                tipoproductoNew.getProductoList().add(producto);
                tipoproductoNew = em.merge(tipoproductoNew);
            }
            if (ubicacionOld != null && !ubicacionOld.equals(ubicacionNew)) {
                ubicacionOld.getProductoList().remove(producto);
                ubicacionOld = em.merge(ubicacionOld);
            }
            if (ubicacionNew != null && !ubicacionNew.equals(ubicacionOld)) {
                ubicacionNew.getProductoList().add(producto);
                ubicacionNew = em.merge(ubicacionNew);
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = producto.getIdP();
                if (findProducto(id) == null) {
                    throw new NonexistentEntityException("The producto with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Producto producto;
            try {
                producto = em.getReference(Producto.class, id);
                producto.getIdP();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The producto with id " + id + " no longer exists.", enfe);
            }
            Tipoproducto tipoproducto = producto.getTipoproducto();
            if (tipoproducto != null) {
                tipoproducto.getProductoList().remove(producto);
                tipoproducto = em.merge(tipoproducto);
            }
            Ubicacion ubicacion = producto.getUbicacion();
            if (ubicacion != null) {
                ubicacion.getProductoList().remove(producto);
                ubicacion = em.merge(ubicacion);
            }
            em.remove(producto);
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Producto> findProductoEntities() {
        return findProductoEntities(true, -1, -1);
    }

    public List<Producto> findProductoEntities(int maxResults, int firstResult) {
        return findProductoEntities(false, maxResults, firstResult);
    }

    private List<Producto> findProductoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Producto.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Producto findProducto(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Producto.class, id);
        } finally {
            em.close();
        }
    }

    public int getProductoCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Producto> rt = cq.from(Producto.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
