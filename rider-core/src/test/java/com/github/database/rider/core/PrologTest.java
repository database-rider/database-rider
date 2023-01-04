package com.github.database.rider.core;

import it.unibo.tuprolog.core.Struct;
import it.unibo.tuprolog.core.TermFormatter;
import it.unibo.tuprolog.core.parsing.TermParser;
import it.unibo.tuprolog.solve.*;
import it.unibo.tuprolog.solve.classic.ClassicSolverFactory;
import it.unibo.tuprolog.theory.Theory;
import it.unibo.tuprolog.theory.parsing.ClausesReader;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import static org.junit.Assert.assertTrue;

public class PrologTest {
    @Test
    public void testPositive() {
        ClausesReader theoryReader = ClausesReader.getWithDefaultOperators();
        SolverFactory solverFactory = ClassicSolverFactory.INSTANCE; // or Solver.getClassic()
        SolutionFormatter defaultSolutionFormatter = SolutionFormatter.of(TermFormatter.prettyExpressions());

        ByteArrayInputStream bais = new ByteArrayInputStream(
                new String("post(1, 'my first post', 'some description').\n" +
                    "comment(1, 1, 'comment 1').\n" +
                    "comment(2, 1, 'comment 2').").getBytes(StandardCharsets.UTF_8)
        );

        Theory theory = theoryReader.readTheory(bais);
        Solver solver = solverFactory.solverWithDefaultBuiltins(theory);

        TermParser termParser = TermParser.withOperators(solver.getOperators());

        Struct query = termParser.parseStruct("post(X, _, _), comment(_, X, 'comment 2')");

        Iterator<Solution> si = solver.solve(query, SolveOptions.allLazilyWithTimeout(1000)).iterator();
        if (si.hasNext()) {
            Solution solution = si.next();
            assertTrue(solution.isYes());
            System.out.println(defaultSolutionFormatter.format(solution));
            System.out.println("----");
        }
    }

    @Test
    public void testNegative() {
        ClausesReader theoryReader = ClausesReader.getWithDefaultOperators();
        SolverFactory solverFactory = ClassicSolverFactory.INSTANCE; // or Solver.getClassic()
        SolutionFormatter defaultSolutionFormatter = SolutionFormatter.of(TermFormatter.prettyExpressions());

        ByteArrayInputStream bais = new ByteArrayInputStream(
                new String("post(1, 'my first post', 'some description').\n" +
                        "comment(1, 1, 'comment 1').\n" +
                        "comment(2, 1, 'comment 2').").getBytes(StandardCharsets.UTF_8)
        );

        Theory theory = theoryReader.readTheory(bais);
        Solver solver = solverFactory.solverWithDefaultBuiltins(theory);

        TermParser termParser = TermParser.withOperators(solver.getOperators());

        Struct query = termParser.parseStruct("post(X, _, _), comment(_, X, 'comment 4')");

        Iterator<Solution> si = solver.solve(query, SolveOptions.allLazilyWithTimeout(1000)).iterator();
        if (si.hasNext()) {
            Solution solution = si.next();
            assertTrue(solution.isNo());
            System.out.println(defaultSolutionFormatter.format(solution));
            System.out.println("----");
        }
    }
}
