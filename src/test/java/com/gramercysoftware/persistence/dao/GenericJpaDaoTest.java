package com.gramercysoftware.persistence.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.persistence.EntityManager;

import org.junit.Ignore;
import org.junit.Test;

import com.gramercysoftware.persistence.DatabaseTestCase;
import com.gramercysoftware.persistence.DummyEntity;
import com.gramercysoftware.persistence.DummyEntity.Type;
import com.gramercysoftware.persistence.util.DefaultEntityManagerUtil;

public class GenericJpaDaoTest extends DatabaseTestCase {
	@Test
	public void insertAndSearchData() {
		GenericJpaDao<DummyEntity,Integer> genericJpaDao = new GenericJpaDao<DummyEntity,Integer>(DummyEntity.class,DefaultEntityManagerUtil.getInstance());
		EntityManager entityManager = genericJpaDao.getEntityManagerUtil().getEntityManager();
		entityManager.getTransaction().begin();
		genericJpaDao.save(createServiceUrl(Type.FOO, "hi", false));
		genericJpaDao.save(createServiceUrl(Type.BAR, "bye", true));
		entityManager.getTransaction().commit();
		
		List<DummyEntity>  serviceUrlList = genericJpaDao.getAll();
		assertEquals(2, serviceUrlList.size());

	}
	
	/**
	 * Inserting without a transaction does nothing.
	 */
	@Test
	@Ignore
	public void insertWithoutTransactionDoesNothing() {
		GenericJpaDao<DummyEntity,Integer> genericJpaDao = new GenericJpaDao<DummyEntity,Integer>(DummyEntity.class,DefaultEntityManagerUtil.getInstance());
		
		List<DummyEntity>  serviceUrlList = genericJpaDao.getAll();
		int countBefore = serviceUrlList.size();
		genericJpaDao.save(createServiceUrl(Type.FOO, "hi", false));
		
		serviceUrlList = genericJpaDao.getAll();
		int countAfter = serviceUrlList.size();
		
		assertEquals(countBefore, countAfter);
	}
	
	/**
	 * Updating works
	 */
	@Test
	public void update() {
		GenericJpaDao<DummyEntity,Integer> genericJpaDao = new GenericJpaDao<DummyEntity,Integer>(DummyEntity.class,DefaultEntityManagerUtil.getInstance());

		EntityManager entityManager;
		entityManager = genericJpaDao.getEntityManagerUtil().getEntityManager();
		entityManager.getTransaction().begin();
		genericJpaDao.save(createServiceUrl(Type.FOO, "hi", false));
		
		entityManager.getTransaction().commit();

		List<DummyEntity>  list = genericJpaDao.getAll();
		assertTrue(!list.get(0).isAvailable());
		
		entityManager = genericJpaDao.getEntityManagerUtil().getEntityManager();
		entityManager.getTransaction().begin();
		list.get(0).setAvailable(true);
		genericJpaDao.save(list.get(0));
		entityManager.getTransaction().commit();

		list = genericJpaDao.getAll();
		assertTrue(list.get(0).isAvailable());
	}
	
	
	/**
	 * Rolling back updates before a commit works.
	 */
	@Test
	public void updateAndRollbackData() {
		GenericJpaDao<DummyEntity,Integer> genericJpaDao = new GenericJpaDao<DummyEntity,Integer>(DummyEntity.class,DefaultEntityManagerUtil.getInstance());

		EntityManager entityManager = genericJpaDao.getEntityManagerUtil().getEntityManager();
		entityManager.getTransaction().begin();

		genericJpaDao.save(createServiceUrl(Type.FOO, "hi", false));
		entityManager.getTransaction().commit();
		
		entityManager.getTransaction().begin();
		List<DummyEntity>  serviceUrlList = genericJpaDao.getAll();
		serviceUrlList.get(0).setAvailable(true);
		entityManager.getTransaction().rollback();

		entityManager.getTransaction().begin();
		serviceUrlList = genericJpaDao.getAll();
		entityManager.getTransaction().commit();

		assertFalse(serviceUrlList.get(0).isAvailable());

	}

	/**
	 * Rollinng back inserts before a commit works.
	 */
	@Test
	public void insertAndRollbackData() {
		GenericJpaDao<DummyEntity,Integer> genericJpaDao = new GenericJpaDao<DummyEntity,Integer>(DummyEntity.class,DefaultEntityManagerUtil.getInstance());

		EntityManager entityManager = genericJpaDao.getEntityManagerUtil().getEntityManager();
		entityManager.getTransaction().begin();

		genericJpaDao.save(createServiceUrl(Type.FOO, "hi", false));
		
		entityManager.getTransaction().rollback();
		
		entityManager.getTransaction().begin();
		List<DummyEntity>  serviceUrlList = genericJpaDao.getAll();
		entityManager.getTransaction().commit();
		assertTrue(serviceUrlList.isEmpty());
		
	}

	/*
	 *  Making Transactional operations when no active transactions active will throw exception
	 */
	@Test(expected=IllegalStateException.class)
	public void rollbackWhenNoActiveTransaction() {
		GenericJpaDao<DummyEntity,Integer> genericJpaDao = new GenericJpaDao<DummyEntity,Integer>(DummyEntity.class,DefaultEntityManagerUtil.getInstance());

		EntityManager entityManager = genericJpaDao.getEntityManagerUtil().getEntityManager();
		entityManager.getTransaction().begin();

		genericJpaDao.save(createServiceUrl(Type.FOO, "hi", false));

		entityManager.getTransaction().commit();

		entityManager.getTransaction().rollback();
		
	}

	/*
	 *  Making Transactional operations when no active transactions active will throw exception 
	 */

	@Test(expected=IllegalStateException.class)
	public void commitWhenNoActiveTransaction() {
		GenericJpaDao<DummyEntity,Integer> genericJpaDao = new GenericJpaDao<DummyEntity,Integer>(DummyEntity.class,DefaultEntityManagerUtil.getInstance());

		EntityManager entityManager = genericJpaDao.getEntityManagerUtil().getEntityManager();
		entityManager.getTransaction().begin();

		genericJpaDao.save(createServiceUrl(Type.FOO, "hi", false));
		entityManager.getTransaction().rollback();

		entityManager.getTransaction().commit();

	}
	
	
	private DummyEntity createServiceUrl(Type type, String message, boolean isAvailable) {
		DummyEntity testEntity = new DummyEntity();
		testEntity.setMessage(message);
		testEntity.setAvailable(isAvailable);
		testEntity.setType(type);
		return testEntity;
	}

}
