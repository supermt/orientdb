package com.orientechnologies.orient.core.storage.index.sbtreebonsai.local.v1;

import com.orientechnologies.common.directmemory.OByteBufferPool;
import com.orientechnologies.common.serialization.types.OLongSerializer;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.serialization.serializer.binary.impl.OLinkSerializer;
import com.orientechnologies.orient.core.storage.cache.OCacheEntry;
import com.orientechnologies.orient.core.storage.cache.OCachePointer;
import com.orientechnologies.orient.core.storage.index.sbtreebonsai.local.OBonsaiBucketPointer;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.TreeSet;

/**
 * @author Andrey Lomakin (a.lomakin-at-orientdb.com)
 * @since 09.08.13
 */
public class OSBTreeBonsaiLeafBucketTest {
  @Test
  public void testInitialization() {
    OByteBufferPool bufferPool = OByteBufferPool.instance();
    ByteBuffer buffer = bufferPool.acquireDirect(true);

    OCachePointer cachePointer = new OCachePointer(buffer, bufferPool, 0, 0);
    cachePointer.incrementReferrer();

    OCacheEntry cacheEntry = new OCacheEntry(0, 0, cachePointer, false);
    cacheEntry.acquireExclusiveLock();

    OSBTreeBonsaiBucketV1<Long, OIdentifiable> treeBucket = new OSBTreeBonsaiBucketV1<>(cacheEntry, 0, true,
        OLongSerializer.INSTANCE, OLinkSerializer.INSTANCE, null);
    Assert.assertEquals(treeBucket.size(), 0);
    Assert.assertTrue(treeBucket.isLeaf());

    treeBucket = new OSBTreeBonsaiBucketV1<>(cacheEntry, 0, OLongSerializer.INSTANCE, OLinkSerializer.INSTANCE, null);
    Assert.assertEquals(treeBucket.size(), 0);
    Assert.assertTrue(treeBucket.isLeaf());
    Assert.assertFalse(treeBucket.getLeftSibling().isValid());
    Assert.assertFalse(treeBucket.getRightSibling().isValid());

    cacheEntry.releaseExclusiveLock();
    cachePointer.decrementReferrer();
  }

  @Test
  public void testSearch() {
    long seed = System.currentTimeMillis();
    System.out.println("testSearch seed : " + seed);

    TreeSet<Long> keys = new TreeSet<>();
    Random random = new Random(seed);

    while (keys.size() < 2 * OSBTreeBonsaiBucketV1.MAX_BUCKET_SIZE_BYTES / OLongSerializer.LONG_SIZE) {
      keys.add(random.nextLong());
    }

    OByteBufferPool bufferPool = OByteBufferPool.instance();
    ByteBuffer buffer = bufferPool.acquireDirect(true);

    OCachePointer cachePointer = new OCachePointer(buffer, bufferPool, 0, 0);
    cachePointer.incrementReferrer();

    OCacheEntry cacheEntry = new OCacheEntry(0, 0, cachePointer, false);
    cacheEntry.acquireExclusiveLock();

    OSBTreeBonsaiBucketV1<Long, OIdentifiable> treeBucket = new OSBTreeBonsaiBucketV1<>(cacheEntry, 0, true,
        OLongSerializer.INSTANCE, OLinkSerializer.INSTANCE, null);

    int index = 0;
    Map<Long, Integer> keyIndexMap = new HashMap<>();
    for (Long key : keys) {
      if (!treeBucket.addEntry(index,
          new OSBTreeBonsaiBucketV1.SBTreeEntry<>(OBonsaiBucketPointer.NULL, OBonsaiBucketPointer.NULL, key,
              new ORecordId(index, index)), true))
        break;
      keyIndexMap.put(key, index);
      index++;
    }

    Assert.assertEquals(treeBucket.size(), keyIndexMap.size());

    for (Map.Entry<Long, Integer> keyIndexEntry : keyIndexMap.entrySet()) {
      int bucketIndex = treeBucket.find(keyIndexEntry.getKey());
      Assert.assertEquals(bucketIndex, (int) keyIndexEntry.getValue());
    }

    cacheEntry.releaseExclusiveLock();
    cachePointer.decrementReferrer();
  }

  @Test
  public void testUpdateValue() {
    long seed = System.currentTimeMillis();
    System.out.println("testUpdateValue seed : " + seed);

    TreeSet<Long> keys = new TreeSet<>();
    Random random = new Random(seed);

    while (keys.size() < 2 * OSBTreeBonsaiBucketV1.MAX_BUCKET_SIZE_BYTES / OLongSerializer.LONG_SIZE) {
      keys.add(random.nextLong());
    }

    OByteBufferPool bufferPool = OByteBufferPool.instance();
    ByteBuffer buffer = bufferPool.acquireDirect(true);

    OCachePointer cachePointer = new OCachePointer(buffer, bufferPool, 0, 0);
    cachePointer.incrementReferrer();

    OCacheEntry cacheEntry = new OCacheEntry(0, 0, cachePointer, false);
    cacheEntry.acquireExclusiveLock();

    OSBTreeBonsaiBucketV1<Long, OIdentifiable> treeBucket = new OSBTreeBonsaiBucketV1<>(cacheEntry, 0, true,
        OLongSerializer.INSTANCE, OLinkSerializer.INSTANCE, null);

    Map<Long, Integer> keyIndexMap = new HashMap<>();
    int index = 0;
    for (Long key : keys) {
      if (!treeBucket.addEntry(index,
          new OSBTreeBonsaiBucketV1.SBTreeEntry<>(OBonsaiBucketPointer.NULL, OBonsaiBucketPointer.NULL, key,
              new ORecordId(index, index)), true))
        break;

      keyIndexMap.put(key, index);
      index++;
    }

    Assert.assertEquals(keyIndexMap.size(), treeBucket.size());

    for (int i = 0; i < treeBucket.size(); i++)
      treeBucket.updateValue(i, new ORecordId(i + 5, i + 5));

    for (Map.Entry<Long, Integer> keyIndexEntry : keyIndexMap.entrySet()) {
      OSBTreeBonsaiBucketV1.SBTreeEntry<Long, OIdentifiable> entry = treeBucket.getEntry(keyIndexEntry.getValue());

      Assert.assertEquals(entry,
          new OSBTreeBonsaiBucketV1.SBTreeEntry<Long, OIdentifiable>(OBonsaiBucketPointer.NULL, OBonsaiBucketPointer.NULL,
              keyIndexEntry.getKey(), new ORecordId(keyIndexEntry.getValue() + 5, keyIndexEntry.getValue() + 5)));
      Assert.assertEquals(keyIndexEntry.getKey(), treeBucket.getKey(keyIndexEntry.getValue()));
    }

    cacheEntry.releaseExclusiveLock();
    cachePointer.decrementReferrer();
  }

  @Test
  public void testShrink() {
    long seed = System.currentTimeMillis();
    System.out.println("testShrink seed : " + seed);

    TreeSet<Long> keys = new TreeSet<>();
    Random random = new Random(seed);

    while (keys.size() < 2 * OSBTreeBonsaiBucketV1.MAX_BUCKET_SIZE_BYTES / OLongSerializer.LONG_SIZE) {
      keys.add(random.nextLong());
    }

    OByteBufferPool bufferPool = OByteBufferPool.instance();
    ByteBuffer buffer = bufferPool.acquireDirect(true);

    OCachePointer cachePointer = new OCachePointer(buffer, bufferPool, 0, 0);
    OCacheEntry cacheEntry = new OCacheEntry(0, 0, cachePointer, false);
    cacheEntry.acquireExclusiveLock();

    cachePointer.incrementReferrer();

    OSBTreeBonsaiBucketV1<Long, OIdentifiable> treeBucket = new OSBTreeBonsaiBucketV1<>(cacheEntry, 0, true,
        OLongSerializer.INSTANCE, OLinkSerializer.INSTANCE, null);

    int index = 0;
    for (Long key : keys) {
      if (!treeBucket.addEntry(index,
          new OSBTreeBonsaiBucketV1.SBTreeEntry<>(OBonsaiBucketPointer.NULL, OBonsaiBucketPointer.NULL, key,
              new ORecordId(index, index)), true))
        break;

      index++;
    }

    int originalSize = treeBucket.size();

    treeBucket.shrink(treeBucket.size() / 2);
    Assert.assertEquals(treeBucket.size(), index / 2);

    index = 0;
    final Map<Long, Integer> keyIndexMap = new HashMap<>();

    Iterator<Long> keysIterator = keys.iterator();
    while (keysIterator.hasNext() && index < treeBucket.size()) {
      Long key = keysIterator.next();
      keyIndexMap.put(key, index);
      index++;
    }

    for (Map.Entry<Long, Integer> keyIndexEntry : keyIndexMap.entrySet()) {
      int bucketIndex = treeBucket.find(keyIndexEntry.getKey());
      Assert.assertEquals(bucketIndex, (int) keyIndexEntry.getValue());
    }

    int keysToAdd = originalSize - treeBucket.size();
    int addedKeys = 0;
    while (keysIterator.hasNext() && index < originalSize) {
      Long key = keysIterator.next();

      if (!treeBucket.addEntry(index,
          new OSBTreeBonsaiBucketV1.SBTreeEntry<>(OBonsaiBucketPointer.NULL, OBonsaiBucketPointer.NULL, key,
              new ORecordId(index, index)), true))
        break;

      keyIndexMap.put(key, index);
      index++;
      addedKeys++;
    }

    for (Map.Entry<Long, Integer> keyIndexEntry : keyIndexMap.entrySet()) {
      OSBTreeBonsaiBucketV1.SBTreeEntry<Long, OIdentifiable> entry = treeBucket.getEntry(keyIndexEntry.getValue());

      Assert.assertEquals(entry,
          new OSBTreeBonsaiBucketV1.SBTreeEntry<Long, OIdentifiable>(OBonsaiBucketPointer.NULL, OBonsaiBucketPointer.NULL,
              keyIndexEntry.getKey(), new ORecordId(keyIndexEntry.getValue(), keyIndexEntry.getValue())));
    }

    Assert.assertEquals(treeBucket.size(), originalSize);
    Assert.assertEquals(addedKeys, keysToAdd);

    cacheEntry.releaseExclusiveLock();
    cachePointer.decrementReferrer();
  }

  @Test
  public void testRemove() {
    long seed = System.currentTimeMillis();
    System.out.println("testRemove seed : " + seed);

    TreeSet<Long> keys = new TreeSet<>();
    Random random = new Random(seed);

    while (keys.size() < 2 * OSBTreeBonsaiBucketV1.MAX_BUCKET_SIZE_BYTES / OLongSerializer.LONG_SIZE) {
      keys.add(random.nextLong());
    }

    OByteBufferPool bufferPool = OByteBufferPool.instance();
    ByteBuffer buffer = bufferPool.acquireDirect(true);

    OCachePointer cachePointer = new OCachePointer(buffer, bufferPool, 0, 0);
    cachePointer.incrementReferrer();

    OCacheEntry cacheEntry = new OCacheEntry(0, 0, cachePointer, false);
    cacheEntry.acquireExclusiveLock();

    OSBTreeBonsaiBucketV1<Long, OIdentifiable> treeBucket = new OSBTreeBonsaiBucketV1<>(cacheEntry, 0, true,
        OLongSerializer.INSTANCE, OLinkSerializer.INSTANCE, null);

    int index = 0;
    for (Long key : keys) {
      if (!treeBucket.addEntry(index,
          new OSBTreeBonsaiBucketV1.SBTreeEntry<>(OBonsaiBucketPointer.NULL, OBonsaiBucketPointer.NULL, key,
              new ORecordId(index, index)), true))
        break;

      index++;
    }

    int originalSize = treeBucket.size();

    int itemsToDelete = originalSize / 2;
    for (int i = 0; i < itemsToDelete; i++) {
      treeBucket.remove(treeBucket.size() - 1);
    }

    Assert.assertEquals(treeBucket.size(), originalSize - itemsToDelete);

    final Map<Long, Integer> keyIndexMap = new HashMap<>();
    Iterator<Long> keysIterator = keys.iterator();

    index = 0;
    while (keysIterator.hasNext() && index < treeBucket.size()) {
      Long key = keysIterator.next();
      keyIndexMap.put(key, index);
      index++;
    }

    for (Map.Entry<Long, Integer> keyIndexEntry : keyIndexMap.entrySet()) {
      int bucketIndex = treeBucket.find(keyIndexEntry.getKey());
      Assert.assertEquals(bucketIndex, (int) keyIndexEntry.getValue());
    }

    int keysToAdd = originalSize - treeBucket.size();
    int addedKeys = 0;
    while (keysIterator.hasNext() && index < originalSize) {
      Long key = keysIterator.next();

      if (!treeBucket.addEntry(index,
          new OSBTreeBonsaiBucketV1.SBTreeEntry<>(OBonsaiBucketPointer.NULL, OBonsaiBucketPointer.NULL, key,
              new ORecordId(index, index)), true))
        break;

      keyIndexMap.put(key, index);
      index++;
      addedKeys++;
    }

    for (Map.Entry<Long, Integer> keyIndexEntry : keyIndexMap.entrySet()) {
      OSBTreeBonsaiBucketV1.SBTreeEntry<Long, OIdentifiable> entry = treeBucket.getEntry(keyIndexEntry.getValue());

      Assert.assertEquals(entry,
          new OSBTreeBonsaiBucketV1.SBTreeEntry<Long, OIdentifiable>(OBonsaiBucketPointer.NULL, OBonsaiBucketPointer.NULL,
              keyIndexEntry.getKey(), new ORecordId(keyIndexEntry.getValue(), keyIndexEntry.getValue())));
    }

    Assert.assertEquals(treeBucket.size(), originalSize);
    Assert.assertEquals(addedKeys, keysToAdd);

    cacheEntry.releaseExclusiveLock();
    cachePointer.decrementReferrer();
  }

  @Test
  public void testSetLeftSibling() {
    OByteBufferPool bufferPool = OByteBufferPool.instance();
    ByteBuffer buffer = bufferPool.acquireDirect(true);

    OCachePointer cachePointer = new OCachePointer(buffer, bufferPool, 0, 0);
    cachePointer.incrementReferrer();

    OCacheEntry cacheEntry = new OCacheEntry(0, 0, cachePointer, false);
    cacheEntry.acquireExclusiveLock();

    OSBTreeBonsaiBucketV1<Long, OIdentifiable> treeBucket = new OSBTreeBonsaiBucketV1<>(cacheEntry, 0, true,
        OLongSerializer.INSTANCE, OLinkSerializer.INSTANCE, null);
    final OBonsaiBucketPointer p = new OBonsaiBucketPointer(123, 8192 * 2, 0);
    treeBucket.setLeftSibling(p);
    Assert.assertEquals(treeBucket.getLeftSibling(), p);

    cacheEntry.releaseExclusiveLock();
    cachePointer.decrementReferrer();
  }

  @Test
  public void testSetRightSibling() {
    OByteBufferPool bufferPool = OByteBufferPool.instance();
    ByteBuffer buffer = bufferPool.acquireDirect(true);

    OCachePointer cachePointer = new OCachePointer(buffer, bufferPool, 0, 0);
    cachePointer.incrementReferrer();

    OCacheEntry cacheEntry = new OCacheEntry(0, 0, cachePointer, false);
    cacheEntry.acquireExclusiveLock();

    OSBTreeBonsaiBucketV1<Long, OIdentifiable> treeBucket = new OSBTreeBonsaiBucketV1<>(cacheEntry, 0, true,
        OLongSerializer.INSTANCE, OLinkSerializer.INSTANCE, null);
    final OBonsaiBucketPointer p = new OBonsaiBucketPointer(123, 8192 * 2, 0);
    treeBucket.setRightSibling(p);
    Assert.assertEquals(treeBucket.getRightSibling(), p);

    cacheEntry.releaseExclusiveLock();
    cachePointer.decrementReferrer();
  }
}
