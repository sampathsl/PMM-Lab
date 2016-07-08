package de.bund.bfr.knime.pmm.combinedfsk.archive;

import junit.framework.TestCase;

import org.junit.Test;

public class ModelNodeTest extends TestCase {

  @Test
  public void test() {
    ModelFiles model = new ModelFiles();
    ModelFilesNode node = new ModelFilesNode(model);
    ModelFiles obtainedModel = node.getModel();
    
    assertEquals(model, obtainedModel);
  }
}
