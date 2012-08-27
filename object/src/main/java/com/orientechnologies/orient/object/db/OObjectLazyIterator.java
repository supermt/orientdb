/*
 * Copyright 2010-2012 Luca Garulli (l.garulli--at--orientechnologies.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.orientechnologies.orient.object.db;

import java.io.Serializable;
import java.util.Iterator;

import javassist.util.proxy.ProxyObject;

import com.orientechnologies.orient.core.db.record.ODatabaseRecord;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.record.ORecordInternal;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.object.enhancement.OObjectEntitySerializer;
import com.orientechnologies.orient.object.enhancement.OObjectProxyMethodHandler;

/**
 * Lazy implementation of Iterator that load the records only when accessed. It keep also track of changes to the source record
 * avoiding to call setDirty() by hand.
 * 
 * @author Luca Garulli (l.garulli--at--orientechnologies.com)
 * 
 */
@SuppressWarnings({ "unchecked" })
public class OObjectLazyIterator<TYPE> implements Iterator<TYPE>, Serializable {
	private static final long									serialVersionUID	= -4012483076050044405L;

	private final ProxyObject									sourceRecord;
	private final ODatabasePojoAbstract<TYPE>	database;
	private final Iterator<? extends Object>	underlying;
	private String														fetchPlan;
	final private boolean											autoConvert2Object;

	public OObjectLazyIterator(final ODatabasePojoAbstract<TYPE> database, final ProxyObject iSourceRecord,
			final Iterator<? extends Object> iIterator, final boolean iConvertToRecord) {
		this.database = database;
		this.sourceRecord = iSourceRecord;
		this.underlying = iIterator;
		autoConvert2Object = iConvertToRecord;
	}

	public TYPE next() {
		return next(fetchPlan);
	}

	public TYPE next(final String iFetchPlan) {
		final Object value = underlying.next();

		if (value == null)
			return null;

		if (value instanceof ORID && autoConvert2Object) {
			TYPE o = database.getUserObjectByRecord(
					(ORecordInternal<?>) ((ODatabaseRecord) database.getUnderlying()).load((ORID) value, iFetchPlan), iFetchPlan);
			((OObjectProxyMethodHandler) (((ProxyObject) o)).getHandler()).setParentObject(sourceRecord);
			return o;
		} else if (value instanceof ODocument && autoConvert2Object) {
			TYPE o = database.getUserObjectByRecord((ODocument) value, iFetchPlan);
			((OObjectProxyMethodHandler) (((ProxyObject) o)).getHandler()).setParentObject(sourceRecord);
			return o;
		}

		if (OObjectEntitySerializer.isToSerialize(value.getClass()))
			return (TYPE) OObjectEntitySerializer.deserializeFieldValue(value.getClass(), value);
		return (TYPE) value;
	}

	public boolean hasNext() {
		return underlying.hasNext();
	}

	public void remove() {
		underlying.remove();
		if (sourceRecord != null)
			((OObjectProxyMethodHandler) sourceRecord.getHandler()).setDirty();
	}

	public String getFetchPlan() {
		return fetchPlan;
	}

	public void setFetchPlan(String fetchPlan) {
		this.fetchPlan = fetchPlan;
	}
}
