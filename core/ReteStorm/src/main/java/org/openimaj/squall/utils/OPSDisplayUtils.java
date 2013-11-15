package org.openimaj.squall.utils;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JTextArea;

import org.openimaj.squall.orchestrate.NamedNode;
import org.openimaj.squall.orchestrate.NamedStream;
import org.openimaj.squall.orchestrate.OrchestratedProductionSystem;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class OPSDisplayUtils {
	/**
	 * Render the OPS as a {@link mxGraph}
	 * @param ops
	 */
	public static void display(OrchestratedProductionSystem ops){
		// Fix "Comparison method violates its general contract!"
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		
		JFrame frame = new JFrame();
		mxGraph mxgraph = new mxGraph();
		
		HashMap<String, Object> cells = new HashMap<String,Object>();
		for (NamedNode<?> vert : ops.vertexSet()) {
			cells.put(vert.getName(),mxgraph.insertVertex(mxgraph.getDefaultParent(), null, vert.getName(), 0, 0, 50, 50));

		}
		for (NamedStream e : ops.edgeSet()) {
			mxgraph.insertEdge(mxgraph.getDefaultParent(), null, e.getName(), cells.get(ops.getEdgeSource(e).getName()), cells.get(ops.getEdgeTarget(e).getName()));
		}
		
		
//		mxFastOrganicLayout layout = new mxFastOrganicLayout(mxgraph);
		mxHierarchicalLayout layout = new mxHierarchicalLayout(mxgraph);

        //set all properties
        layout.setInterHierarchySpacing(100);
        layout.setIntraCellSpacing(100);
        layout.setDisableEdgeStyle(true);

        //layout graph
        layout.execute(mxgraph.getDefaultParent());
		
		
		final mxGraphComponent cmp = new mxGraphComponent(mxgraph);
		final JTextArea outputArea = new JTextArea(10, 40);
		cmp.getGraphControl().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Object cell = cmp.getCellAt(e.getX(), e.getY());
				if (cell != null && cell instanceof mxCell) {
					mxCell mxc = ((mxCell) cell);
					if (mxc.isVertex()) {

						String n = mxc.getValue().toString();
						outputArea.setText(n);
					}
				}
			}
		});
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(cmp, BorderLayout.CENTER);
		frame.getContentPane().add(outputArea, BorderLayout.SOUTH);
		frame.pack();
		frame.setVisible(true);
	}

}
