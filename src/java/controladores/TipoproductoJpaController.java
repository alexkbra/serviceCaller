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
import entidades.Producto;
import entidades.Tipoproducto;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;

/**
 *
 * @author andrea
 */
public class TipoproductoJpaController implements Serializable {

    public TipoproductoJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Tipoproducto tipoproducto) throws RollbackFailureException, Exception {
        if (tipoproducto.getProductoList() == null) {
            tipoproducto.setProductoList(new ArrayList<Producto>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            List<Producto> attachedProductoList = new ArrayList<Producto>();
            for (Producto productoListProductoToAttach : tipoproducto.getProductoList()) {
                productoListProductoToAttach = em.getReference(productoListProductoToAttach.getClass(), productoListProductoToAttach.getIdP());
                attachedProductoList.add(productoListProductoToAttach);
            }
            tipoproducto.setProductoList(attachedProductoList);
            em.persist(tipoproducto);
            for (Producto productoListProducto : tipoproducto.getProductoList()) {
                Tipoproducto oldTipoproductoOfProductoListProducto = productoListProducto.getTipoproducto();
                productoListProducto.setTipoproducto(tipoproducto);
                productoListProducto = em.merge(productoListProducto);
                if (oldTipoproductoOfProductoListProducto != null) {
                    oldTipoproductoOfProductoListProducto.getProductoList().remove(productoListProducto);
                    oldTipoproductoOfProductoListProducto = em.merge(oldTipoproductoOfProductoListProducto);
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

    public void edit(Tipoproducto tipoproducto) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Tipoproducto persistentTipoproducto = em.find(Tipoproducto.class, tipoproducto.getId());
            List<Producto> productoListOld = persistentTipoproducto.getProductoList();
            List<Producto> productoListNew = tipoproducto.getProductoList();
            List<String> illegalOrphanMessages = null;
            for (Producto productoListOldProducto : productoListOld) {
                if (!productoListNew.contains(productoListOldProducto)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Producto " + productoListOldProducto + " since its tipoproducto field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            List<Producto> attachedProductoListNew = new ArrayList<Producto>();
            for (Producto productoListNewProductoToAttach : productoListNew) {
                productoListNewProductoToAttach = em.getReference(productoListNewProductoToAttach.getClass(), productoListNewProductoToAttach.getIdP());
                attachedProductoListNew.add(productoListNewProductoToAttach);
            }
            productoListNew = attachedProductoListNew;
            tipoproducto.setProductoList(productoListNew);
            tipoproducto = em.merge(tipoproducto);
            for (Producto productoListNewProducto : productoListNew) {
                if (!productoListOld.contains(productoListNewProducto)) {
                    Tipoproducto oldTipoproductoOfProductoListNewProducto = productoListNewProducto.getTipoproducto();
                    productoListNewProducto.setTipoproducto(tipoproducto);
                    productoListNewProducto = em.merge(productoListNewProducto);
                    if (oldTipoproductoOfProductoListNewProducto != null && !oldTipoproductoOfProductoListNewProducto.equals(tipoproducto)) {
                        oldTipoproductoOfProductoListNewProducto.getProductoList().remove(productoListNewProducto);
                        oldTipoproductoOfProductoListNewProducto = em.merge(oldTipoproductoOfProductoListNewProducto);
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
                Integer id = tipoproducto.getId();
                if (findTipoproducto(id) == null) {
                    throw new NonexistentEntityException("The tipoproducto with id " + id + " no longer exists.");
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
            Tipoproducto tipoproducto;
            try {
                tipoproducto = em.getReference(Tipoproducto.class, id);
                tipoproducto.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The tipoproducto with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<Producto> productoListOrphanCheck = tipoproducto.getProductoList();
            for (Producto productoListOrphanCheckProducto : productoListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Tipoproducto (" + tipoproducto + ") cannot be destroyed since the Producto " + productoListOrphanCheckProducto + " in its productoList field has a non-nullable tipoproducto field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(tipoproducto);
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

    public List<Tipoproducto> findTipoproductoEntities() {
        return findTipoproductoEntities(true, -1, -1);
    }

    public List<Tipoproducto> findTipoproductoEntities(int maxResults, int firstResult) {
        return findTipoproductoEntities(false, maxResults, firstResult);
    }

    private List<Tipoproducto> findTipoproductoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Tipoproducto.class));
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

    public Tipoproducto findTipoproducto(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Tipoproducto.class, id);
        } finally {
            em.close();
        }
    }

    public int getTipoproductoCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Tipoproducto> rt = cq.from(Tipoproducto.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
