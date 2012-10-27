/*******************************************************************************
 * Copyright (c) 2012 Fabian Steeg. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *******************************************************************************/

package ccw.editors.outline;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.zest.dot.DotGraph;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Tests for the {@link #ClojureVisualization} class.
 *
 * @author fsteeg (Fabian Steeg)
 */
@RunWith(value = Parameterized.class)
public class ClojureVisualizationTests {

  private ClojureVisualization cv = new ClojureVisualization();
  private Shell shell = new Shell();
  private String i, o;

  @Parameters
  public static Collection<Object[]> data() {
    Object[][] tests = {
        // different data structures: vector, list, map, set
        { "[1 2 3 4]",/* -> */"GraphModel {5 nodes, 4 connections}" },
        { "(1 2 3 4)",/* -> */"GraphModel {5 nodes, 4 connections}" },
        { "{1 2 3 4}",/* -> */"GraphModel {7 nodes, 6 connections}" },
        { "[{:a :b}]",/* -> */"GraphModel {5 nodes, 4 connections}" },
        { "#{1 2 3}",/* -> */"GraphModel {4 nodes, 3 connections}" },
        // nested data structures: vectors in maps, vectors in vectors
        { "{[1] [4]}",/* -> */"GraphModel {6 nodes, 5 connections}" },
        { "{1 [3 4]}",/* -> */"GraphModel {6 nodes, 5 connections}" },
        { "[1 [3 4]]",/* -> */"GraphModel {5 nodes, 4 connections}" },
        { "[1 {3 4}]",/* -> */"GraphModel {6 nodes, 5 connections}" },
        { "(1 2 '(3 4))",/* -> */"GraphModel {8 nodes, 7 connections}" },
        // some edge cases: nil, empty strucures
        { "[nil 3 4]",/* -> */"GraphModel {4 nodes, 3 connections}" },
        { "[]",/* -> */"GraphModel {1 nodes, 0 connections}" },
        // treating identical value as separate nodes
        { "[\"a\" \"b\"]",/* -> */"GraphModel {3 nodes, 2 connections}" },
        { "[\"a\" \"a\"]",/* -> */"GraphModel {3 nodes, 2 connections}" },
        { "[:a :a]",/* -> */"GraphModel {3 nodes, 2 connections}" },
        { "[1 1]",/* -> */"GraphModel {3 nodes, 2 connections}" },
    // TODO
    // { "nil",/* -> */"GraphModel {1 nodes, 0 connections}" },
    // { "[nil nil]",/* -> */"GraphModel {3 nodes, 2 connections}" },
    };
    return Arrays.asList(tests);
  }

  public ClojureVisualizationTests(String i, String o) {
    this.i = i;
    this.o = o;
  }

  @Test
  public void graphForClojure() {
    assertEquals(i + " -> " + o, o, graphForClojure(i).toString());
  }

  private DotGraph graphForClojure(String clojure) {
    return new DotGraph(cv.dotForClojure(clojure), shell, SWT.NONE);
  }

}
