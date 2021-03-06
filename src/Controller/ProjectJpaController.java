/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import Controller.exceptions.NonexistentEntityException;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import Model.HeadquarterInfo;
import Model.PositieEmployer;
import Model.Project;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author Benny
 */
public class ProjectJpaController implements Serializable {

    public ProjectJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Project project) {
        if (project.getPositieEmployerCollection() == null) {
            project.setPositieEmployerCollection(new ArrayList<PositieEmployer>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            HeadquarterInfo headquarterid = project.getHeadquarterid();
            if (headquarterid != null) {
                headquarterid = em.getReference(headquarterid.getClass(), headquarterid.getHeadquarterid());
                project.setHeadquarterid(headquarterid);
            }
            Collection<PositieEmployer> attachedPositieEmployerCollection = new ArrayList<PositieEmployer>();
            for (PositieEmployer positieEmployerCollectionPositieEmployerToAttach : project.getPositieEmployerCollection()) {
                positieEmployerCollectionPositieEmployerToAttach = em.getReference(positieEmployerCollectionPositieEmployerToAttach.getClass(), positieEmployerCollectionPositieEmployerToAttach.getPositieEmployerPK());
                attachedPositieEmployerCollection.add(positieEmployerCollectionPositieEmployerToAttach);
            }
            project.setPositieEmployerCollection(attachedPositieEmployerCollection);
            em.persist(project);
            if (headquarterid != null) {
                headquarterid.getProjectCollection().add(project);
                headquarterid = em.merge(headquarterid);
            }
            for (PositieEmployer positieEmployerCollectionPositieEmployer : project.getPositieEmployerCollection()) {
                Project oldProjectidOfPositieEmployerCollectionPositieEmployer = positieEmployerCollectionPositieEmployer.getProjectid();
                positieEmployerCollectionPositieEmployer.setProjectid(project);
                positieEmployerCollectionPositieEmployer = em.merge(positieEmployerCollectionPositieEmployer);
                if (oldProjectidOfPositieEmployerCollectionPositieEmployer != null) {
                    oldProjectidOfPositieEmployerCollectionPositieEmployer.getPositieEmployerCollection().remove(positieEmployerCollectionPositieEmployer);
                    oldProjectidOfPositieEmployerCollectionPositieEmployer = em.merge(oldProjectidOfPositieEmployerCollectionPositieEmployer);
                }
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Project project) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Project persistentProject = em.find(Project.class, project.getProjectid());
            HeadquarterInfo headquarteridOld = persistentProject.getHeadquarterid();
            HeadquarterInfo headquarteridNew = project.getHeadquarterid();
            Collection<PositieEmployer> positieEmployerCollectionOld = persistentProject.getPositieEmployerCollection();
            Collection<PositieEmployer> positieEmployerCollectionNew = project.getPositieEmployerCollection();
            if (headquarteridNew != null) {
                headquarteridNew = em.getReference(headquarteridNew.getClass(), headquarteridNew.getHeadquarterid());
                project.setHeadquarterid(headquarteridNew);
            }
            Collection<PositieEmployer> attachedPositieEmployerCollectionNew = new ArrayList<PositieEmployer>();
            for (PositieEmployer positieEmployerCollectionNewPositieEmployerToAttach : positieEmployerCollectionNew) {
                positieEmployerCollectionNewPositieEmployerToAttach = em.getReference(positieEmployerCollectionNewPositieEmployerToAttach.getClass(), positieEmployerCollectionNewPositieEmployerToAttach.getPositieEmployerPK());
                attachedPositieEmployerCollectionNew.add(positieEmployerCollectionNewPositieEmployerToAttach);
            }
            positieEmployerCollectionNew = attachedPositieEmployerCollectionNew;
            project.setPositieEmployerCollection(positieEmployerCollectionNew);
            project = em.merge(project);
            if (headquarteridOld != null && !headquarteridOld.equals(headquarteridNew)) {
                headquarteridOld.getProjectCollection().remove(project);
                headquarteridOld = em.merge(headquarteridOld);
            }
            if (headquarteridNew != null && !headquarteridNew.equals(headquarteridOld)) {
                headquarteridNew.getProjectCollection().add(project);
                headquarteridNew = em.merge(headquarteridNew);
            }
            for (PositieEmployer positieEmployerCollectionOldPositieEmployer : positieEmployerCollectionOld) {
                if (!positieEmployerCollectionNew.contains(positieEmployerCollectionOldPositieEmployer)) {
                    positieEmployerCollectionOldPositieEmployer.setProjectid(null);
                    positieEmployerCollectionOldPositieEmployer = em.merge(positieEmployerCollectionOldPositieEmployer);
                }
            }
            for (PositieEmployer positieEmployerCollectionNewPositieEmployer : positieEmployerCollectionNew) {
                if (!positieEmployerCollectionOld.contains(positieEmployerCollectionNewPositieEmployer)) {
                    Project oldProjectidOfPositieEmployerCollectionNewPositieEmployer = positieEmployerCollectionNewPositieEmployer.getProjectid();
                    positieEmployerCollectionNewPositieEmployer.setProjectid(project);
                    positieEmployerCollectionNewPositieEmployer = em.merge(positieEmployerCollectionNewPositieEmployer);
                    if (oldProjectidOfPositieEmployerCollectionNewPositieEmployer != null && !oldProjectidOfPositieEmployerCollectionNewPositieEmployer.equals(project)) {
                        oldProjectidOfPositieEmployerCollectionNewPositieEmployer.getPositieEmployerCollection().remove(positieEmployerCollectionNewPositieEmployer);
                        oldProjectidOfPositieEmployerCollectionNewPositieEmployer = em.merge(oldProjectidOfPositieEmployerCollectionNewPositieEmployer);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = project.getProjectid();
                if (findProject(id) == null) {
                    throw new NonexistentEntityException("The project with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Project project;
            try {
                project = em.getReference(Project.class, id);
                project.getProjectid();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The project with id " + id + " no longer exists.", enfe);
            }
            HeadquarterInfo headquarterid = project.getHeadquarterid();
            if (headquarterid != null) {
                headquarterid.getProjectCollection().remove(project);
                headquarterid = em.merge(headquarterid);
            }
            Collection<PositieEmployer> positieEmployerCollection = project.getPositieEmployerCollection();
            for (PositieEmployer positieEmployerCollectionPositieEmployer : positieEmployerCollection) {
                positieEmployerCollectionPositieEmployer.setProjectid(null);
                positieEmployerCollectionPositieEmployer = em.merge(positieEmployerCollectionPositieEmployer);
            }
            em.remove(project);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Project> findProjectEntities() {
        return findProjectEntities(true, -1, -1);
    }

    public List<Project> findProjectEntities(int maxResults, int firstResult) {
        return findProjectEntities(false, maxResults, firstResult);
    }

    private List<Project> findProjectEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Project.class));
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

    public Project findProject(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Project.class, id);
        } finally {
            em.close();
        }
    }

    public int getProjectCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Project> rt = cq.from(Project.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
