/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package controladores;

import controladores.exceptions.IllegalOrphanException;
import controladores.exceptions.NonexistentEntityException;
import controladores.exceptions.RollbackFailureException;
import entidades.Bodega;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import entidades.Usuario;
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
public class BodegaJpaController implements Serializable {

    public BodegaJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Bodega bodega) throws RollbackFailureException, Exception {
        if (bodega.getUbicacionList() == null) {
            bodega.setUbicacionList(new ArrayList<Ubicacion>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Usuario usuario = bodega.getUsuario();
            if (usuario != null) {
                usuario = em.getReference(usuario.getClass(), usuario.getIdentificacion());
                bodega.setUsuario(usuario);
            }
            List<Ubicacion> attachedUbicacionList = new ArrayList<Ubicacion>();
            for (Ubicacion ubicacionListUbicacionToAttach : bodega.getUbicacionList()) {
                ubicacionListUbicacionToAttach = em.getReference(ubicacionListUbicacionToAttach.getClass(), ubicacionListUbicacionToAttach.getId());
                attachedUbicacionList.add(ubicacionListUbicacionToAttach);
            }
            bodega.setUbicacionList(attachedUbicacionList);
            em.persist(bodega);
            if (usuario != null) {
                usuario.getBodegaList().add(bodega);
                usuario = em.merge(usuario);
            }
            for (Ubicacion ubicacionListUbicacion : bodega.getUbicacionList()) {
                Bodega oldBodegaOfUbicacionListUbicacion = ubicacionListUbicacion.getBodega();
                ubicacionListUbicacion.setBodega(bodega);
                ubicacionListUbicacion = em.merge(ubicacionListUbicacion);
                if (oldBodegaOfUbicacionListUbicacion != null) {
                    oldBodegaOfUbicacionListUbicacion.getUbicacionList().remove(ubicacionListUbicacion);
                    oldBodegaOfUbicacionListUbicacion = em.merge(oldBodegaOfUbicacionListUbicacion);
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

    public void edit(Bodega bodega) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Bodega persistentBodega = em.find(Bodega.class, bodega.getId());
            Usuario usuarioOld = persistentBodega.getUsuario();
            Usuario usuarioNew = bodega.getUsuario();
            List<Ubicacion> ubicacionListOld = persistentBodega.getUbicacionList();
            List<Ubicacion> ubicacionListNew = bodega.getUbicacionList();
            List<String> illegalOrphanMessages = null;
            for (Ubicacion ubicacionListOldUbicacion : ubicacionListOld) {
                if (!ubicacionListNew.contains(ubicacionListOldUbicacion)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Ubicacion " + ubicacionListOldUbicacion + " since its bodega field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (usuarioNew != null) {
                usuarioNew = em.getReference(usuarioNew.getClass(), usuarioNew.getIdentificacion());
                bodega.setUsuario(usuarioNew);
            }
            List<Ubicacion> attachedUbicacionListNew = new ArrayList<Ubicacion>();
            for (Ubicacion ubicacionListNewUbicacionToAttach : ubicacionListNew) {
                ubicacionListNewUbicacionToAttach = em.getReference(ubicacionListNewUbicacionToAttach.getClass(), ubicacionListNewUbicacionToAttach.getId());
                attachedUbicacionListNew.add(ubicacionListNewUbicacionToAttach);
            }
            ubicacionListNew = attachedUbicacionListNew;
            bodega.setUbicacionList(ubicacionListNew);
            bodega = em.merge(bodega);
            if (usuarioOld != null && !usuarioOld.equals(usuarioNew)) {
                usuarioOld.getBodegaList().remove(bodega);
                usuarioOld = em.merge(usuarioOld);
            }
            if (usuarioNew != null && !usuarioNew.equals(usuarioOld)) {
                usuarioNew.getBodegaList().add(bodega);
                usuarioNew = em.merge(usuarioNew);
            }
            for (Ubicacion ubicacionListNewUbicacion : ubicacionListNew) {
                if (!ubicacionListOld.contains(ubicacionListNewUbicacion)) {
                    Bodega oldBodegaOfUbicacionListNewUbicacion = ubicacionListNewUbicacion.getBodega();
                    ubicacionListNewUbicacion.setBodega(bodega);
                    ubicacionListNewUbicacion = em.merge(ubicacionListNewUbicacion);
                    if (oldBodegaOfUbicacionListNewUbicacion != null && !oldBodegaOfUbicacionListNewUbicacion.equals(bodega)) {
                        oldBodegaOfUbicacionListNewUbicacion.getUbicacionList().remove(ubicacionListNewUbicacion);
                        oldBodegaOfUbicacionListNewUbicacion = em.merge(oldBodegaOfUbicacionListNewUbicacion);
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
                Integer id = bodega.getId();
                if (findBodega(id) == null) {
                    throw new NonexistentEntityException("The bodega with id " + id + " no longer exists.");
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
            Bodega bodega;
            try {
                bodega = em.getReference(Bodega.class, id);
                bodega.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The bodega with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<Ubicacion> ubicacionListOrphanCheck = bodega.getUbicacionList();
            for (Ubicacion ubicacionListOrphanCheckUbicacion : ubicacionListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Bodega (" + bodega + ") cannot be destroyed since the Ubicacion " + ubicacionListOrphanCheckUbicacion + " in its ubicacionList field has a non-nullable bodega field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Usuario usuario = bodega.getUsuario();
            if (usuario != null) {
                usuario.getBodegaList().remove(bodega);
                usuario = em.merge(usuario);
            }
            em.remove(bodega);
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

    public List<Bodega> findBodegaEntities() {
        return findBodegaEntities(true, -1, -1);
    }

    public List<Bodega> findBodegaEntities(int maxResults, int firstResult) {
        return findBodegaEntities(false, maxResults, firstResult);
    }

    private List<Bodega> findBodegaEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Bodega.class));
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

    public Bodega findBodega(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Bodega.class, id);
        } finally {
            em.close();
        }
    }

    public int getBodegaCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Bodega> rt = cq.from(Bodega.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
