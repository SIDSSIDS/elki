package experimentalcode.erich.visualization;

import java.util.BitSet;
import java.util.HashMap;

import org.apache.batik.util.SVGConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PrettyMarkers extends MinimalMarkers {
  /**
   * Bit set of markers we have defined.
   */
  HashMap<SVGPlot,BitSet> definedMarkers = new HashMap<SVGPlot,BitSet>();
  
  /**
   * Constructor
   */
  public PrettyMarkers() {
    // nothing to do.
  }

  /**
   * Draw an marker used in scatter plots. If you intend to use the markers multiple times,
   * you should consider using the {@link #useMarker} method instead, which exploits the SVG
   * features of symbol definition and use
   * 
   * @param document containing document
   * @param parent parent node
   * @param x position
   * @param y position
   * @param style marker style (enumerated)
   * @param size size
   */
  public static void plotMarker(Document document, Element parent, double x, double y, int style, double size) {
    // TODO: add more styles.
    String colorstr = getColor(style);

    switch(style % 8){
    case 0: {
      // + cross
      Element line1 = SVGUtil.svgElement(document, parent, "line");
      SVGUtil.setAtt(line1,"x1", x);
      SVGUtil.setAtt(line1,"y1", y - size / 2);
      SVGUtil.setAtt(line1,"x2", x);
      SVGUtil.setAtt(line1,"y2", y + size / 2);
      SVGUtil.setAtt(line1,"style", "stroke:" + colorstr + "; stroke-width:" + SVGUtil.fmt(size / 6));
      Element line2 = SVGUtil.svgElement(document, parent, "line");
      SVGUtil.setAtt(line2,"x1", x - size / 2);
      SVGUtil.setAtt(line2,"y1", y);
      SVGUtil.setAtt(line2,"x2", x + size / 2);
      SVGUtil.setAtt(line2,"y2", y);
      SVGUtil.setAtt(line2,"style", "stroke:" + colorstr + "; stroke-width: " + SVGUtil.fmt(size / 6));
      break;
    }
    case 1: {
      // X cross
      Element line1 = SVGUtil.svgElement(document, parent, "line");
      SVGUtil.setAtt(line1,"x1", x - size / 2);
      SVGUtil.setAtt(line1,"y1", y - size / 2);
      SVGUtil.setAtt(line1,"x2", x + size / 2);
      SVGUtil.setAtt(line1,"y2", y + size / 2);
      SVGUtil.setAtt(line1,"style", "stroke:" + colorstr + "; stroke-width: " + SVGUtil.fmt(size / 6));
      Element line2 = SVGUtil.svgElement(document, parent, "line");
      SVGUtil.setAtt(line2,"x1", x - size / 2);
      SVGUtil.setAtt(line2,"y1", y + size / 2);
      SVGUtil.setAtt(line2,"x2", x + size / 2);
      SVGUtil.setAtt(line2,"y2", y - size / 2);
      SVGUtil.setAtt(line2,"style", "stroke:" + colorstr + "; stroke-width: " + SVGUtil.fmt(size / 6));
      break;
    }
    case 2: {
      // O filled circle
      Element circ = SVGUtil.svgElement(document, parent, "circle");
      SVGUtil.setAtt(circ,"cx", x);
      SVGUtil.setAtt(circ,"cy", y);
      SVGUtil.setAtt(circ,"r", size / 2);
      SVGUtil.setAtt(circ,"style", "fill:" + colorstr);
      break;
    }
    case 3: {
      // [] filled rectangle
      Element rect = SVGUtil.svgElement(document, parent, "rect");
      SVGUtil.setAtt(rect,"x",x - size / 2);
      SVGUtil.setAtt(rect,"y",y - size / 2);
      SVGUtil.setAtt(rect,"width",size);
      SVGUtil.setAtt(rect,"height",size);
      SVGUtil.setAtt(rect,"style","fill:" + colorstr);
      break;
    }
    case 4: {
      // <> filled diamond
      Element rect = SVGUtil.svgElement(document, parent, "rect");
      SVGUtil.setAtt(rect,"x",x - size / 2);
      SVGUtil.setAtt(rect,"y",y - size / 2);
      SVGUtil.setAtt(rect,"width",size);
      SVGUtil.setAtt(rect,"height",size);
      SVGUtil.setAtt(rect,"style","fill:" + colorstr);
      SVGUtil.setAtt(rect,"transform","rotate(45," + SVGUtil.fmt(x) + "," + SVGUtil.fmt(y) + ")");
      break;
    }
    case 5: {
      // O hollow circle
      Element circ = SVGUtil.svgElement(document, parent, "circle");
      SVGUtil.setAtt(circ,"cx",x);
      SVGUtil.setAtt(circ,"cy",y);
      SVGUtil.setAtt(circ,"r",size / 2);
      SVGUtil.setAtt(circ,"style","fill: none; stroke: " + colorstr + "; stroke-width: " + SVGUtil.fmt(size / 6));
      break;
    }
    case 6: {
      // [] hollow rectangle
      Element rect = SVGUtil.svgElement(document, parent, "rect");
      SVGUtil.setAtt(rect,"x",x - size / 2);
      SVGUtil.setAtt(rect,"y",y - size / 2);
      SVGUtil.setAtt(rect,"width",size);
      SVGUtil.setAtt(rect,"height",size);
      SVGUtil.setAtt(rect,"style","fill: none; stroke: " + colorstr + "; stroke-width: " + SVGUtil.fmt(size / 6));
      break;
    }
    case 7: {
      // <> hollow diamond
      Element rect = SVGUtil.svgElement(document, parent, "rect");
      SVGUtil.setAtt(rect,"x",x - size / 2);
      SVGUtil.setAtt(rect,"y",y - size / 2);
      SVGUtil.setAtt(rect,"width",size);
      SVGUtil.setAtt(rect,"height",size);
      SVGUtil.setAtt(rect,"style","fill: none; stroke: " + colorstr + "; stroke-width: " + SVGUtil.fmt(size / 6));
      SVGUtil.setAtt(rect,"transform","rotate(45," + SVGUtil.fmt(x) + "," + SVGUtil.fmt(y) + ")");
      break;
    }
    }
  }

  @Override
  public void useMarker(SVGPlot plot, Element parent, double x, double y, int style, double size) {
    if(!definedMarkers.containsKey(plot)) {
      definedMarkers.put(plot, new BitSet());
    }
    if(!definedMarkers.get(plot).get(style)) {
      Element symbol = plot.svgElement(plot.getDefs(), "symbol");
      SVGUtil.setAtt(symbol,"id","s" + style);
      SVGUtil.setAtt(symbol,"viewBox","-1 -1 2 2");
      plotMarker(plot.getDocument(), symbol, 0, 0, style, 2);
      definedMarkers.get(plot).set(style);
    }
    Element use = plot.svgElement(parent, "use");
    use.setAttributeNS(SVGConstants.XLINK_NAMESPACE_URI, SVGConstants.XLINK_HREF_QNAME, "#s" + style);
    SVGUtil.setAtt(use,"x",x - size);
    SVGUtil.setAtt(use,"y",y - size);
    SVGUtil.setAtt(use,"width",size * 2);
    SVGUtil.setAtt(use,"height",size * 2);
  }
}
