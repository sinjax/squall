package org.openimaj.squall.utils;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JTextArea;

import org.openimaj.squall.orchestrate.NamedNode;
import org.openimaj.squall.orchestrate.NamedStream;
import org.openimaj.squall.orchestrate.OrchestratedProductionSystem;

import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.layout.mxGraphLayout;
import com.mxgraph.layout.mxOrganicLayout;
import com.mxgraph.layout.mxPartitionLayout;
import com.mxgraph.layout.mxStackLayout;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.layout.orthogonal.mxOrthogonalLayout;
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
		final Map<String,String> details = new HashMap<String,String>();
		
		HashMap<String, Object> cells = new HashMap<String,Object>();
		for (NamedNode<?> vert : ops.vertexSet()) {
			cells.put(vert.getName(),mxgraph.insertVertex(mxgraph.getDefaultParent(), null, vert.getName(), 0, 0, 50, 50));
			if(vert.isFunction()){
				details.put(vert.getName(),vert.getFunction().toString());
			} else if(vert.isSource()){
				details.put(vert.getName(),vert.getSource().toString());
			}

		}
		for (NamedStream e : ops.edgeSet()) {
			NamedNode<?> edgeSource = ops.getEdgeSource(e);
			NamedNode<?> edgeTarget = ops.getEdgeTarget(e);
			Object edgeSourceCell = cells.get(edgeSource.getName());
			Object edgeTargetCell = cells.get(edgeTarget.getName());
			if(edgeSource.isReentrantSource()) {
				edgeTargetCell = null;
			}
			mxgraph.insertEdge(
				mxgraph.getDefaultParent(), null, e.getName(), 
				edgeSourceCell, 
				edgeTargetCell
			);
		}
		
		
//		mxFastOrganicLayout layout = new mxFastOrganicLayout(mxgraph);
		mxGraphLayout layout = null;
		layout = hierarchicalLayout(mxgraph);
        

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

						String n = mxc.getValue().toString() + "\n" + details.get(mxc.getValue().toString());
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

	private static mxGraphLayout organicLayout(mxGraph mxgraph) {
//		mxPartitionLayout layout = new mxPartitionLayout(mxgraph, true, 100);
//		mxOrganicLayout layout = new mxOrganicLayout(mxgraph);
//		layout.setMinDistanceLimit(100);
		mxFastOrganicLayout layout = new mxFastOrganicLayout(mxgraph);
		layout.setMinDistanceLimit(100);
		
		return layout;
	}

	private static mxGraphLayout hierarchicalLayout(mxGraph mxgraph) {
		mxHierarchicalLayout hlay = new mxHierarchicalLayout(mxgraph);   //set all properties
        hlay.setInterHierarchySpacing(100);
		hlay.setIntraCellSpacing(100);
		hlay.setDisableEdgeStyle(true);
		
		mxGraphLayout layout = hlay;
		return layout;
	}

}
