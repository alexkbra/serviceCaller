/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package controladores;

import controladores.exceptions.IllegalOrphanException;
import controladores.exceptions.NonexistentEntityException;
import controladores.exceptions.RollbackFailureException;
import entidades.Perfil;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import entidades.Permiso;
import java.util.ArrayList;
import java.util.List;
import entidades.Usuario;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;

/**
 *
 * @author andrea
 */
public class PerfilJpaController implements Serializable {

    public PerfilJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Perfil perfil) throws RollbackFailureException, Exception {
        if (perfil.getPermisoList() == null) {
            perfil.setPermisoList(new ArrayList<Permiso>());
        }
        if (perfil.getUsuarioList() == null) {
            perfil.setUsuarioList(new ArrayList<Usuario>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            List<Permiso> attachedPermisoList = new ArrayList<Permiso>();
            for (Permiso permisoListPermisoToAttach : perfil.getPermisoList()) {
                permisoListPermisoToAttach = em.getReference(permisoListPermisoToAttach.getClass(), permisoListPermisoToAttach.getId());
                attachedPermisoList.add(permisoListPermisoToAttach);
            }
            perfil.setPermisoList(attachedPermisoList);
            List<Usuario> attachedUsuarioList = new ArrayList<Usuario>();
            for (Usuario usuarioListUsuarioToAttach : perfil.getUsuarioList()) {
                usuarioListUsuarioToAttach = em.getReference(usuarioListUsuarioToAttach.getClass(), usuarioListUsuarioToAttach.getIdentificacion());
                attachedUsuarioList.add(usuarioListUsuarioToAttach);
            }
            perfil.setUsuarioList(attachedUsuarioList);
            em.persist(perfil);
            for (Permiso permisoListPermiso : perfil.getPermisoList()) {
                permisoListPermiso.getPerfilList().add(perfil);
                permisoListPermiso = em.merge(permisoListPermiso);
            }
            for (Usuario usuarioListUsuario : perfil.getUsuarioList()) {
                Perfil oldPerfilOfUsuarioListUsuario = usuarioListUsuario.getPerfil();
                usuarioListUsuario.setPerfil(perfil);
                usuarioListUsuario = em.merge(usuarioListUsuario);
                if (oldPerfilOfUsuarioListUsuario != null) {
                    oldPerfilOfUsuarioListUsuario.getUsuarioList().remove(usuarioListUsuario);
                    oldPerfilOfUsuarioListUsuario = em.merge(oldPerfilOfUsuarioListUsuario);
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

    public void edit(Perfil perfil) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Perfil persistentPerfil = em.find(Perfil.class, perfil.getId());
            List<Permiso> permisoListOld = persistentPerfil.getPermisoList();
            List<Permiso> permisoListNew = perfil.getPermisoList();
            List<Usuario> usuarioListOld = persistentPerfil.getUsuarioList();
            List<Usuario> usuarioListNew = perfil.getUsuarioList();
            List<String> illegalOrphanMessages = null;
            for (Usuario usuarioListOldUsuario : usuarioListOld) {
                if (!usuarioListNew.contains(usuarioListOldUsuario)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Usuario " + usuarioListOldUsuario + " since its perfil field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            List<Permiso> attachedPermisoListNew = new ArrayList<Permiso>();
            for (Permiso permisoListNewPermisoToAttach : permisoListNew) {
                permisoListNewPermisoToAttach = em.getReference(permisoListNewPermisoToAttach.getClass(), permisoListNewPermisoToAttach.getId());
                attachedPermisoListNew.add(permisoListNewPermisoToAttach);
            }
            permisoListNew = attachedPermisoListNew;
            perfil.setPermisoList(permisoListNew);
            List<Usuario> attachedUsuarioListNew = new ArrayList<Usuario>();
            for (Usuario usuarioListNewUsuarioToAttach : usuarioListNew) {
                usuarioListNewUsuarioToAttach = em.getReference(usuarioListNewUsuarioToAttach.getClass(), usuarioListNewUsuarioToAttach.getIdentificacion());
                attachedUsuarioListNew.add(usuarioListNewUsuarioToAttach);
            }
            usuarioListNew = attachedUsuarioListNew;
            perfil.setUsuarioList(usuarioListNew);
            perfil = em.merge(perfil);
            for (Permiso permisoListOldPermiso : permisoListOld) {
                if (!permisoListNew.contains(permisoListOldPermiso)) {
                    permisoListOldPermiso.getPerfilList().remove(perfil);
                    permisoListOldPermiso = em.merge(permisoListOldPermiso);
                }
            }
            for (Permiso permisoListNewPermiso : permisoListNew) {
                if (!permisoListOld.contains(permisoListNewPermiso)) {
                    permisoListNewPermiso.getPerfilList().add(perfil);
                    permisoListNewPermiso = em.merge(permisoListNewPermiso);
                }
            }
            for (Usuario usuarioListNewUsuario : usuarioListNew) {
                if (!usuarioListOld.contains(usuarioListNewUsuario)) {
                    Perfil oldPerfilOfUsuarioListNewUsuario = usuarioListNewUsuario.getPerfil();
                    usuarioListNewUsuario.setPerfil(perfil);
                    usuarioListNewUsuario = em.merge(usuarioListNewUsuario);
                    if (oldPerfilOfUsuarioListNewUsuario != null && !oldPerfilOfUsuarioListNewUsuario.equals(perfil)) {
                        oldPerfilOfUsuarioListNewUsuario.getUsuarioList().remove(usuarioListNewUsuario);
                        oldPerfilOfUsuarioListNewUsuario = em.merge(oldPerfilOfUsuarioListNewUsuario);
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
                Integer id = perfil.getId();
                if (findPerfil(id) == null) {
                    throw new NonexistentEntityException("The perfil with id " + id + " no longer exists.");
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
            Perfil perfil;
            try {
                perfil = em.getReference(Perfil.class, id);
                perfil.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The perfil with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<Usuario> usuarioListOrphanCheck = perfil.getUsuarioList();
            for (Usuario usuarioListOrphanCheckUsuario : usuarioListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Perfil (" + perfil + ") cannot be destroyed since the Usuario " + usuarioListOrphanCheckUsuario + " in its usuarioList field has a non-nullable perfil field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            List<Permiso> permisoList = perfil.getPermisoList();
            for (Permiso permisoListPermiso : permisoList) {
                permisoListPermiso.getPerfilList().remove(perfil);
                permisoListPermiso = em.merge(permisoListPermiso);
            }
            em.remove(perfil);
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

    public List<Perfil> findPerfilEntities() {
        return findPerfilEntities(true, -1, -1);
    }

    public List<Perfil> findPerfilEntities(int maxResults, int firstResult) {
        return findPerfilEntities(false, maxResults, firstResult);
    }

    private List<Perfil> findPerfilEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Perfil.class));
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

    public Perfil findPerfil(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Perfil.class, id);
        } finally {
            em.close();
        }
    }

    public int getPerfilCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Perfil> rt = cq.from(Perfil.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
