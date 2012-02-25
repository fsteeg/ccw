/*******************************************************************************
 * Copyright (c) 2012 Fabian Steeg. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *******************************************************************************/

package ccw.editors.outline;

import java.io.StringReader;
import java.util.Iterator;

import org.eclipse.ui.part.IPageSite;
import org.eclipse.zest.internal.dot.ZestGraphView;

import clojure.lang.APersistentMap;
import clojure.lang.APersistentSet;
import clojure.lang.APersistentVector;
import clojure.lang.ASeq;
import clojure.lang.LineNumberingPushbackReader;
import clojure.lang.LispReader;
import clojure.lang.LispReader.ReaderException;

/**
 * Visualize Clojure code with Zest.
 *
 * @author fsteeg (Fabian Steeg)
 */
class ClojureVisualization {

  private IPageSite site;

  ClojureVisualization(IPageSite site) {
    this.site = site;
  }

  void graphFor(String text) {
    if (!text.trim().isEmpty()) {
      try {
        Object o = LispReader.read(new LineNumberingPushbackReader(
            new StringReader(text)), false, new Object(), false);
        String dot = String.format("digraph {rankdir=TD; \n%s\n}",
            toDot(new StringBuilder(), o));
        ZestGraphView view = (ZestGraphView) site.getPage().findView(
            ZestGraphView.ID);
        if (view != null)
          view.setGraph(dot, true);
      } catch (ReaderException e) {
        // ignore, probably a syntax error
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  private StringBuilder toDot(StringBuilder dot, Object o) {
    String id = id(o);
    return /**/
    (o instanceof ASeq) ? drawSeq(dot, o, id)
        : (o instanceof APersistentVector) ? drawVec(dot, o, id)
            : (o instanceof APersistentSet) ? drawSet(dot, o, id)
                : (o instanceof APersistentMap) ? drawMap(dot, o, id) : dot;
  }

  private StringBuilder drawSeq(StringBuilder dot, Object result, String id) {
    dot.append(node(id, "( )"));
    ASeq coll = (ASeq) result;
    for (int i = 0; i < coll.count(); i++)
      dot = draw(dot, id, coll.get(i));
    return dot;
  }

  private StringBuilder drawVec(StringBuilder dot, Object result, String id) {
    dot.append(node(id, "[ ]"));
    APersistentVector coll = (APersistentVector) result;
    for (int i = 0; i < coll.count(); i++)
      dot = draw(dot, id, coll.get(i));
    return dot;
  }

  private StringBuilder drawSet(StringBuilder dot, Object result, String id) {
    dot.append(node(id, "#{ }"));
    APersistentSet coll = (APersistentSet) result;
    Iterator<?> iter = coll.iterator();
    for (int i = 0; i < coll.count(); i++)
      dot = draw(dot, id, coll.get(iter.next()));
    return dot;
  }

  private StringBuilder drawMap(StringBuilder dot, Object result, String id) {
    dot.append(node(id, "{ }"));
    APersistentMap coll = (APersistentMap) result;
    Iterator<?> iter = coll.keySet().iterator();
    for (int i = 0; i < coll.count(); i++) {
      Object key = iter.next();
      Object val = coll.get(key);
      String kv = id(key) + id(val);
      dot.append(node(kv, " : ")).append(edge(id, kv));
      dot = draw(dot, kv, key);
      dot = draw(dot, kv, val);
    }
    return dot;
  }

  private StringBuilder draw(StringBuilder dot, String source, Object o) {
    String target = id(o);
    return isCollection(o) ? dot = toDot(dot.append(edge(source, target)), o)
        : dot.append(node(target, o.toString())).append(edge(source, target));
  }

  private String edge(String source, String target) {
    return String.format("%s->\"%s\"\n", source, target);
  }

  private String node(String id, String label) {
    return String.format("%s[label=\"%s\"]\n", id, label.replace("\"", "\\\""));
  }

  private boolean isCollection(Object o) {
    return o instanceof ASeq || o instanceof APersistentVector
        || o instanceof APersistentMap || o instanceof APersistentSet;
  }

  private String id(Object o) {
    return Integer.toString(System.identityHashCode(o)); // FIXME ID for nums
  }
}
