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
import entidades.Perfil;
import entidades.Bodega;
import entidades.Usuario;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;

/**
 *
 * @author andrea
 */
public class UsuarioJpaController implements Serializable {

    public UsuarioJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Usuario usuario) throws RollbackFailureException, Exception {
        if (usuario.getBodegaList() == null) {
            usuario.setBodegaList(new ArrayList<Bodega>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Perfil perfil = usuario.getPerfil();
            if (perfil != null) {
                perfil = em.getReference(perfil.getClass(), perfil.getId());
                usuario.setPerfil(perfil);
            }
            List<Bodega> attachedBodegaList = new ArrayList<Bodega>();
            for (Bodega bodegaListBodegaToAttach : usuario.getBodegaList()) {
                bodegaListBodegaToAttach = em.getReference(bodegaListBodegaToAttach.getClass(), bodegaListBodegaToAttach.getId());
                attachedBodegaList.add(bodegaListBodegaToAttach);
            }
            usuario.setBodegaList(attachedBodegaList);
            em.persist(usuario);
            if (perfil != null) {
                perfil.getUsuarioList().add(usuario);
                perfil = em.merge(perfil);
            }
            for (Bodega bodegaListBodega : usuario.getBodegaList()) {
                Usuario oldUsuarioOfBodegaListBodega = bodegaListBodega.getUsuario();
                bodegaListBodega.setUsuario(usuario);
                bodegaListBodega = em.merge(bodegaListBodega);
                if (oldUsuarioOfBodegaListBodega != null) {
                    oldUsuarioOfBodegaListBodega.getBodegaList().remove(bodegaListBodega);
                    oldUsuarioOfBodegaListBodega = em.merge(oldUsuarioOfBodegaListBodega);
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

    public void edit(Usuario usuario) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Usuario persistentUsuario = em.find(Usuario.class, usuario.getIdentificacion());
            Perfil perfilOld = persistentUsuario.getPerfil();
            Perfil perfilNew = usuario.getPerfil();
            List<Bodega> bodegaListOld = persistentUsuario.getBodegaList();
            List<Bodega> bodegaListNew = usuario.getBodegaList();
            List<String> illegalOrphanMessages = null;
            for (Bodega bodegaListOldBodega : bodegaListOld) {
                if (!bodegaListNew.contains(bodegaListOldBodega)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Bodega " + bodegaListOldBodega + " since its usuario field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (perfilNew != null) {
                perfilNew = em.getReference(perfilNew.getClass(), perfilNew.getId());
                usuario.setPerfil(perfilNew);
            }
            List<Bodega> attachedBodegaListNew = new ArrayList<Bodega>();
            for (Bodega bodegaListNewBodegaToAttach : bodegaListNew) {
                bodegaListNewBodegaToAttach = em.getReference(bodegaListNewBodegaToAttach.getClass(), bodegaListNewBodegaToAttach.getId());
                attachedBodegaListNew.add(bodegaListNewBodegaToAttach);
            }
            bodegaListNew = attachedBodegaListNew;
            usuario.setBodegaList(bodegaListNew);
            usuario = em.merge(usuario);
            if (perfilOld != null && !perfilOld.equals(perfilNew)) {
                perfilOld.getUsuarioList().remove(usuario);
                perfilOld = em.merge(perfilOld);
            }
            if (perfilNew != null && !perfilNew.equals(perfilOld)) {
                perfilNew.getUsuarioList().add(usuario);
                perfilNew = em.merge(perfilNew);
            }
            for (Bodega bodegaListNewBodega : bodegaListNew) {
                if (!bodegaListOld.contains(bodegaListNewBodega)) {
                    Usuario oldUsuarioOfBodegaListNewBodega = bodegaListNewBodega.getUsuario();
                    bodegaListNewBodega.setUsuario(usuario);
                    bodegaListNewBodega = em.merge(bodegaListNewBodega);
                    if (oldUsuarioOfBodegaListNewBodega != null && !oldUsuarioOfBodegaListNewBodega.equals(usuario)) {
                        oldUsuarioOfBodegaListNewBodega.getBodegaList().remove(bodegaListNewBodega);
                        oldUsuarioOfBodegaListNewBodega = em.merge(oldUsuarioOfBodegaListNewBodega);
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
                Integer id = usuario.getIdentificacion();
                if (findUsuario(id) == null) {
                    throw new NonexistentEntityException("The usuario with id " + id + " no longer exists.");
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
            Usuario usuario;
            try {
                usuario = em.getReference(Usuario.class, id);
                usuario.getIdentificacion();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The usuario with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<Bodega> bodegaListOrphanCheck = usuario.getBodegaList();
            for (Bodega bodegaListOrphanCheckBodega : bodegaListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Usuario (" + usuario + ") cannot be destroyed since the Bodega " + bodegaListOrphanCheckBodega + " in its bodegaList field has a non-nullable usuario field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Perfil perfil = usuario.getPerfil();
            if (perfil != null) {
                perfil.getUsuarioList().remove(usuario);
                perfil = em.merge(perfil);
            }
            em.remove(usuario);
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

    public List<Usuario> findUsuarioEntities() {
        return findUsuarioEntities(true, -1, -1);
    }

    public List<Usuario> findUsuarioEntities(int maxResults, int firstResult) {
        return findUsuarioEntities(false, maxResults, firstResult);
    }

    private List<Usuario> findUsuarioEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Usuario.class));
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

    public Usuario findUsuario(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Usuario.class, id);
        } finally {
            em.close();
        }
    }

    public int getUsuarioCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Usuario> rt = cq.from(Usuario.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
