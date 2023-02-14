package com.github.database.rider.core.assertion;

import it.unibo.tuprolog.core.parsing.TermParser;
import it.unibo.tuprolog.solve.Solution;
import it.unibo.tuprolog.solve.SolveOptions;
import it.unibo.tuprolog.solve.Solver;
import it.unibo.tuprolog.solve.SolverFactory;
import it.unibo.tuprolog.solve.classic.ClassicSolverFactory;
import it.unibo.tuprolog.theory.Theory;
import it.unibo.tuprolog.theory.parsing.ClausesReader;
import org.dbunit.DatabaseUnitException;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class to do Prolog-style assertions on database data based on tuProlog.
 *
 * @see <a href="https://apice.unibo.it/xwiki/bin/view/Tuprolog/">https://apice.unibo.it/xwiki/bin/view/Tuprolog/</a>
 * <p>
 * Alternative could be <a href="https://jpl7.org/TutorialJavaCallsProlog">https://jpl7.org/TutorialJavaCallsProlog</a> which uses SWI Prolog
 */
public class PrologAssert {

    private static final Logger log = LoggerFactory.getLogger(PrologAssert.class);

    public static void compareProlog(IDataSet current, IDataSet expected, String[] tableNames, Long prologTimeout) throws DatabaseUnitException {
        StringBuilder sbDatabaseFacts = new StringBuilder();
        List<String> queryTerms = new ArrayList<>();

        for (String tableName : tableNames) {
            ITable expectedTable = expected.getTable(tableName);
            ITable actualTable = current.getTable(tableName);

            sbDatabaseFacts.append(createActualTableFacts(actualTable));

            queryTerms.addAll(createQueryTermsFromExpectedTable(expectedTable, actualTable));
        }

        final String databaseFacts = sbDatabaseFacts.toString();

        final String databaseQuery = String.join(",", queryTerms);

        solve(prologTimeout, databaseFacts, databaseQuery);
    }


    /**
     * Transform table rows/entries to a fact per row.
     *
     * @param actualTable actual table
     * @return list of facts as string separated by newlines.
     * @throws DataSetException
     */
    private static String createActualTableFacts(final ITable actualTable) throws DataSetException {
        StringBuilder sbTableFacts = new StringBuilder();
        for (int i = 0; i < actualTable.getRowCount(); i++) {
            sbTableFacts.append(tableToRelationName(actualTable));
            sbTableFacts.append("(");
            int finalI = i;
            sbTableFacts.append(
                    Arrays.stream(actualTable.getTableMetaData().getColumns())
                            .map(column -> {
                                try {
                                    return actualTable.getValue(finalI, column.getColumnName());
                                } catch (DataSetException e) {
                                    throw new RuntimeException(e);
                                }
                            })
                            .map(o -> "'" + o + "'")
                            .collect(Collectors.joining(","))
            );
            sbTableFacts.append(").\n");
        }
        return sbTableFacts.toString();
    }


    /**
     * Create query terms from provided expected table.
     * <br/>
     * Columns missed out in the expectation are replaced with <code>_</code> (Prolog wildcard), present values are transformed to string literals.
     * <br/>
     *
     * @param expectedTable specified expectation
     * @param actualTable   actual table, needed to ensure all columns are present and their order is the same
     * @return list of query terms
     * @throws DataSetException
     */
    private static List<String> createQueryTermsFromExpectedTable(ITable expectedTable, ITable actualTable) throws DataSetException {
        List<String> queryTerms = new ArrayList<>();
        for (int i = 0; i < expectedTable.getRowCount(); i++) {
            StringBuilder sbQuery = new StringBuilder();
            sbQuery.append(tableToRelationName(expectedTable));
            sbQuery.append("(");
            int finalI = i;
            sbQuery.append(
                    Arrays.stream(actualTable.getTableMetaData().getColumns())
                            .map(column -> {
                                try {
                                    return expectedTable.getValue(finalI, column.getColumnName());
                                } catch (DataSetException e) {
                                    throw new RuntimeException(e);
                                }
                            })
                            // FIXME: simple #toString() may not work well with all datatypes
                            .map(o -> o == null ? "_" : (o.toString().startsWith("$$") && o.toString().endsWith("$$")) ? o.toString().replaceAll("\\$\\$", "").toUpperCase() : "'" + o + "'")
                            .collect(Collectors.joining(","))
            );
            sbQuery.append(")");
            queryTerms.add(sbQuery.toString());
        }
        return queryTerms;
    }

    private static String tableToRelationName(ITable expectedTable) {
        return expectedTable.getTableMetaData().getTableName().toLowerCase();
    }

    private static void solve(Long prologTimeout, String databaseFacts, String databaseQuery) throws DataSetException {
        log.debug("Attempting to solve, facts = {}, query = {}", databaseFacts, databaseQuery);

        ClausesReader theoryReader = ClausesReader.getWithDefaultOperators();
        SolverFactory solverFactory = ClassicSolverFactory.INSTANCE; // or Solver.getClassic()

        ByteArrayInputStream bais = new ByteArrayInputStream(
                databaseFacts.getBytes(StandardCharsets.UTF_8)
        );

        Theory theory = theoryReader.readTheory(bais);
        Solver solver = solverFactory.solverWithDefaultBuiltins(theory);

        TermParser termParser = TermParser.withOperators(solver.getOperators());

        it.unibo.tuprolog.core.Struct query = termParser.parseStruct(databaseQuery);

        Iterator<Solution> si = solver.solve(query, SolveOptions.allLazilyWithTimeout(prologTimeout)).iterator();

        if (!si.hasNext() || si.next().isNo()) {
            throw new DataSetException("Could not find a solution to theory: " + theory + " given query: " + query);
        }
    }

}
