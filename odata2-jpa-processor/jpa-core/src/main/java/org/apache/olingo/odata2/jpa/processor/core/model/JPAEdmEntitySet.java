/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 ******************************************************************************/
package org.apache.olingo.odata2.jpa.processor.core.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.api.edm.provider.EntitySet;
import org.apache.olingo.odata2.api.edm.provider.EntityType;
import org.apache.olingo.odata2.jpa.processor.api.access.JPAEdmBuilder;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAModelException;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmEntitySetView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmEntityTypeView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmSchemaView;
import org.apache.olingo.odata2.jpa.processor.core.access.model.JPAEdmNameBuilder;

public class JPAEdmEntitySet extends JPAEdmBaseViewImpl implements JPAEdmEntitySetView {

  private EntitySet currentEntitySet = null;
  private List<EntitySet> consistentEntitySetList = null;
  private JPAEdmEntityTypeView entityTypeView = null;
  private JPAEdmSchemaView schemaView;

  public JPAEdmEntitySet(final JPAEdmSchemaView view) {
    super(view);
    schemaView = view;
  }

  @Override
  public JPAEdmBuilder getBuilder() {
    if (builder == null) {
      builder = new JPAEdmEntitySetBuilder();
    }

    return builder;
  }

  @Override
  public EntitySet getEdmEntitySet() {
    return currentEntitySet;
  }

  @Override
  public List<EntitySet> getConsistentEdmEntitySetList() {
    return consistentEntitySetList;
  }

  @Override
  public JPAEdmEntityTypeView getJPAEdmEntityTypeView() {
    return entityTypeView;
  }

  @Override
  public void clean() {
    currentEntitySet = null;
    consistentEntitySetList = null;
    entityTypeView = null;
    isConsistent = false;
  }

  private class JPAEdmEntitySetBuilder implements JPAEdmBuilder {

    @Override
    public void build() throws ODataJPAModelException, ODataJPARuntimeException {

      if (consistentEntitySetList == null) {
        consistentEntitySetList = new ArrayList<EntitySet>();
      }

      entityTypeView = new JPAEdmEntityType(schemaView);
      entityTypeView.getBuilder().build();

      if (entityTypeView.isConsistent() && entityTypeView.getConsistentEdmEntityTypes() != null) {

        String nameSpace = schemaView.getEdmSchema().getNamespace();
        for (EntityType entityType : entityTypeView.getConsistentEdmEntityTypes()) {

          currentEntitySet = new EntitySet();
          currentEntitySet.setEntityType(new FullQualifiedName(nameSpace, entityType.getName()));
          JPAEdmNameBuilder.build(JPAEdmEntitySet.this, entityTypeView);
          consistentEntitySetList.add(currentEntitySet);

        }
        isConsistent = true;
      } else {
        isConsistent = false;
        return;
      }

    }

  }

}
