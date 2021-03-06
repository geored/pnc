/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.facade.rsql;

import org.jboss.pnc.dto.BuildConfiguration;
import org.jboss.pnc.dto.Project;
import org.jboss.pnc.model.BuildEnvironment;
import org.jboss.pnc.model.BuildEnvironment_;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildRecord_;
import org.junit.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public class RSQLPredicateProducerTest {

    RSQLPredicateProducerImpl producer = new RSQLPredicateProducerImpl();

    @Test
    public void testCriteriaPredicate(){
        org.jboss.pnc.spi.datastore.repositories.api.Predicate<BuildRecord> criteriaPredicate
                = producer.getCriteriaPredicate(this::toPath, "id==4");

        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Root<BuildRecord> root = mock(Root.class);
        Path<Integer> idPath = mock(Path.class);

        when(root.get(BuildRecord_.id)).thenReturn(idPath);

        criteriaPredicate.apply(root, null, cb);

        Mockito.verify(cb).equal(idPath, "4");
    }

    @Test
    public void testCriteriaPredicateEmbeded(){
        org.jboss.pnc.spi.datastore.repositories.api.Predicate<BuildRecord> criteriaPredicate
                = producer.getCriteriaPredicate(this::toPath, "environment.name==fooEnv");

        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Root<BuildRecord> root = mock(Root.class);
        Join<BuildRecord, BuildEnvironment> join = mock(Join.class);
        Path<String> namePath = mock(Path.class);

        when(root.join(BuildRecord_.buildEnvironment)).thenReturn(join);
        when(join.get(BuildEnvironment_.name)).thenReturn(namePath);

        criteriaPredicate.apply(root, null, cb);

        Mockito.verify(cb).equal(namePath, "fooEnv");
    }

    @Test
    public void testCriteriaPredicateUnknownQuery(){
        org.jboss.pnc.spi.datastore.repositories.api.Predicate<BuildRecord> criteriaPredicate
                = producer.getCriteriaPredicate(this::toPath, "fieldThatDoesNotExists==\"FooBar\"");

        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Root<BuildRecord> root = mock(Root.class);

        try{
            criteriaPredicate.apply(root, null, cb);
            fail("Exception expected");
        }catch(RuntimeException ex){
            // ok
        }
    }

    private Path<?> toPath(From<?, BuildRecord> from, RSQLSelectorPath selector) {
        switch (selector.getElement()) {
            case "id": return from.get(BuildRecord_.id);
            case "environment":
                return toPathEnvironment(from.join(BuildRecord_.buildEnvironment), selector.next());
            default:
                throw new IllegalArgumentException("Unknown RSQL selector " + selector.getElement());
        }
    }

    private Path<?> toPathEnvironment(From<?, BuildEnvironment> from, RSQLSelectorPath selector) {
        switch (selector.getElement()) {
            case "name": return from.get(BuildEnvironment_.name);
            default:
                throw new IllegalArgumentException("Unknown RSQL selector " + selector.getElement());
        }
    }


    @Test
    public void testStreamPredicate(){
        Predicate<BuildConfiguration> streamPredicate = producer.getStreamPredicate("name==\"FooBar\"");

        BuildConfiguration fooBar = BuildConfiguration.builder().name("FooBar").build();
        BuildConfiguration fooBaz = BuildConfiguration.builder().name("FooBaz").build();

        List<BuildConfiguration> filtered = Arrays.asList(fooBar, fooBaz).stream()
                .filter(streamPredicate)
                .collect(Collectors.toList());

        assertEquals(1, filtered.size());
        assertEquals("FooBar", filtered.get(0).getName());
    }

    @Test
    public void testStreamPredicateEmbeded(){
        Predicate<BuildConfiguration> streamPredicate = producer.getStreamPredicate("project.name==\"Bar Project\"");

        Project projBar = Project.builder().name("Bar Project").build();
        Project projBaz = Project.builder().name("Baz Project").build();
        BuildConfiguration fooBar = BuildConfiguration.builder().project(projBar).build();
        BuildConfiguration fooBaz = BuildConfiguration.builder().project(projBaz).build();

        List<BuildConfiguration> filtered = Arrays.asList(fooBar, fooBaz).stream()
                .filter(streamPredicate)
                .collect(Collectors.toList());

        assertEquals(1, filtered.size());
        assertEquals("Bar Project", filtered.get(0).getProject().getName());
    }

    @Test
    public void testStreamPredicateUnknownQuery(){
        Predicate<BuildConfiguration> streamPredicate = producer.getStreamPredicate("fieldThatDoesNotExists==\"FooBar\"");

        BuildConfiguration fooBar = BuildConfiguration.builder().name("FooBar").build();
        BuildConfiguration fooBaz = BuildConfiguration.builder().name("FooBaz").build();

        try{
            List<BuildConfiguration> filtered = Arrays.asList(fooBar, fooBaz).stream()
                    .filter(streamPredicate)
                    .collect(Collectors.toList());
            fail("Exception expected");
        }catch(RuntimeException ex){
            // ok
        }
    }
}
