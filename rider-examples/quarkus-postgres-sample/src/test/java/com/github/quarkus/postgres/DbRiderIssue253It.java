package com.github.quarkus.postgres;

import com.github.database.rider.cdi.api.DBRider;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.transaction.Transactional;

@QuarkusTest
@QuarkusTestResource(PostgresTestResource.class)
@DBRider
public class DbRiderIssue253It {

  @Inject
  DummyRepository dummyRepository;

  @Test
  @Transactional
  @ExpectedDataSet(value = "datasets/DbRiderIssue253Test/expected.yml")
  void expectedDataSetWorksForTablesWithOidDataType() {
    dummyRepository.persist(new DummyEntity(1));
  }
}
