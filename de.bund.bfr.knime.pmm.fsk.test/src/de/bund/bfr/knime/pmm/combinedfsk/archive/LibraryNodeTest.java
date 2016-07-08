package de.bund.bfr.knime.pmm.combinedfsk.archive;

import junit.framework.TestCase;

public class LibraryNodeTest extends TestCase {

  public void test() {
    LibraryNode node = new LibraryNode("triangle");
    assertEquals("triangle", node.getLibrary());
    
    LibraryNode copyNode = new LibraryNode(node.getElement());
    assertEquals("triangle", copyNode.getLibrary());
  }
}
