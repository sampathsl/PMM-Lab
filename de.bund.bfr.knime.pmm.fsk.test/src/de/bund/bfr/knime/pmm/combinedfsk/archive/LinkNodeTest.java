package de.bund.bfr.knime.pmm.combinedfsk.archive;

import junit.framework.TestCase;

import org.junit.Test;

public class LinkNodeTest extends TestCase {

  @Test
  public void test() {
    Link origLink = new Link(1, "x", 2, "y");
    LinkNode node = new LinkNode(origLink);
    Link link = node.getLink();
    
    assertEquals(origLink, link);
  }
}
