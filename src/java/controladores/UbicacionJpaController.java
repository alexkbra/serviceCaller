/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package controladores;

import controladores.exceptions.IllegalOrphanException;
import controladores.exceptions.NonexistentEntityException;
import controladores.exceptions.RollbackFailureException;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import entidades.Bodega;
import entidades.Producto;
import entidades.Ubicacion;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;

/**
 *
 * @author andrea
 */
public class UbicacionJpaController implements Serializable {

    public UbicacionJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Ubicacion ubicacion) throws RollbackFailureException, Exception {
        if (ubicacion.getProductoList() == null) {
            ubicacion.setProductoList(new ArrayList<Producto>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Bodega bodega = ubicacion.getBodega();
            if (bodega != null) {
                bodega = em.getReference(bodega.getClass(), bodega.getId());
                ubicacion.setBodega(bodega);
            }
            List<Producto> attachedProductoList = new ArrayList<Producto>();
            for (Producto productoListProductoToAttach : ubicacion.getProductoList()) {
                productoListProductoToAttach = em.getReference(productoListProductoToAttach.getClass(), productoListProductoToAttach.getIdP());
                attachedProductoList.add(productoListProductoToAttach);
            }
            ubicacion.setProductoList(attachedProductoList);
            em.persist(ubicacion);
            if (bodega != null) {
                bodega.getUbicacionList().add(ubicacion);
                bodega = em.merge(bodega);
            }
            for (Producto productoListProducto : ubicacion.getProductoList()) {
                Ubicacion oldUbicacionOfProductoListProducto = productoListProducto.getUbicacion();
                productoListProducto.setUbicacion(ubicacion);
                productoListProducto = em.merge(productoListProducto);
                if (oldUbicacionOfProductoListProducto != null) {
                    oldUbicacionOfProductoListProducto.getProductoList().remove(productoListProducto);
                    oldUbicacionOfProductoListProducto = em.merge(oldUbicacionOfProductoListProducto);
                }
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

    public void edit(Ubicacion ubicacion) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Ubicacion persistentUbicacion = em.find(Ubicacion.class, ubicacion.getId());
            Bodega bodegaOld = persistentUbicacion.getBodega();
            Bodega bodegaNew = ubicacion.getBodega();
            List<Producto> productoListOld = persistentUbicacion.getProductoList();
            List<Producto> productoListNew = ubicacion.getProductoList();
            List<String> illegalOrphanMessages = null;
            for (Producto productoListOldProducto : productoListOld) {
                if (!productoListNew.contains(productoListOldProducto)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Producto " + productoListOldProducto + " since its ubicacion field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (bodegaNew != null) {
                bodegaNew = em.getReference(bodegaNew.getClass(), bodegaNew.getId());
                ubicacion.setBodega(bodegaNew);
            }
            List<Producto> attachedProductoListNew = new ArrayList<Producto>();
            for (Producto productoListNewProductoToAttach : productoListNew) {
                productoListNewProductoToAttach = em.getReference(productoListNewProductoToAttach.getClass(), productoListNewProductoToAttach.getIdP());
                attachedProductoListNew.add(productoListNewProductoToAttach);
            }
            productoListNew = attachedProductoListNew;
            ubicacion.setProductoList(productoListNew);
            ubicacion = em.merge(ubicacion);
            if (bodegaOld != null && !bodegaOld.equals(bodegaNew)) {
                bodegaOld.getUbicacionList().remove(ubicacion);
                bodegaOld = em.merge(bodegaOld);
            }
            if (bodegaNew != null && !bodegaNew.equals(bodegaOld)) {
                bodegaNew.getUbicacionList().add(ubicacion);
                bodegaNew = em.merge(bodegaNew);
            }
            for (Producto productoListNewProducto : productoListNew) {
                if (!productoListOld.contains(productoListNewProducto)) {
                    Ubicacion oldUbicacionOfProductoListNewProducto = productoListNewProducto.getUbicacion();
                    productoListNewProducto.setUbicacion(ubicacion);
                    productoListNewProducto = em.merge(productoListNewProducto);
                    if (oldUbicacionOfProductoListNewProducto != null && !oldUbicacionOfProductoListNewProducto.equals(ubicacion)) {
                        oldUbicacionOfProductoListNewProducto.getProductoList().remove(productoListNewProducto);
                        oldUbicacionOfProductoListNewProducto = em.merge(oldUbicacionOfProductoListNewProducto);
                    }
                }
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
                Integer id = ubicacion.getId();
                if (findUbicacion(id) == null) {
                    throw new NonexistentEntityException("The ubicacion with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Ubicacion ubicacion;
            try {
                ubicacion = em.getReference(Ubicacion.class, id);
                ubicacion.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The ubicacion with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<Producto> productoListOrphanCheck = ubicacion.getProductoList();
            for (Producto productoListOrphanCheckProducto : productoListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Ubicacion (" + ubicacion + ") cannot be destroyed since the Producto " + productoListOrphanCheckProducto + " in its productoList field has a non-nullable ubicacion field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Bodega bodega = ubicacion.getBodega();
            if (bodega != null) {
                bodega.getUbicacionList().remove(ubicacion);
                bodega = em.merge(bodega);
            }
            em.remove(ubicacion);
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

    public List<Ubicacion> findUbicacionEntities() {
        return findUbicacionEntities(true, -1, -1);
    }

    public List<Ubicacion> findUbicacionEntities(int maxResults, int firstResult) {
        return findUbicacionEntities(false, maxResults, firstResult);
    }

    private List<Ubicacion> findUbicacionEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Ubicacion.class));
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

    public Ubicacion findUbicacion(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Ubicacion.class, id);
        } finally {
            em.close();
        }
    }

    public int getUbicacionCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Ubicacion> rt = cq.from(Ubicacion.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
