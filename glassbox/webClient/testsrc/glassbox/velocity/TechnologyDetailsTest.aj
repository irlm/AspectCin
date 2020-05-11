package glassbox.velocity;

import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.springframework.ui.velocity.VelocityEngineFactory;

public class TechnologyDetailsTest extends TestCase {
    private VelocityEngine engine;
    private Context context;
    private Writer writer;
    
    protected void setUp() {
        VelocityEngineFactory factory = new VelocityEngineFactory();
        Map velocityPropertiesMap = new HashMap();
        velocityPropertiesMap.put("resource.loader", "file");
        velocityPropertiesMap.put("file.resource.loader.path", "web/velocity");
        factory.setVelocityPropertiesMap(velocityPropertiesMap);
        
        engine = factory.createVelocityEngine();
        context = new VelocityContext();
        writer = new StringWriter();
    }
    
    public void testNormalTechnologyDetails() {
        context.put("", "");
        engine.mergeTemplate("templates/normal_technology_details.vm", context, writer);
    }
    
    public void testSlowTechnologyDetails() {
        context.put("", "");
        engine.mergeTemplate("templates/slow_technology_details.vm", context, writer);
    }
    
    public void testFailTechnologyDetails() {
        context.put("", "");
        engine.mergeTemplate("templates/fail_technology_details.vm", context, writer);
    }
    
    private static aspect TestHelper {
        declare soft: Exception: within(TechnologyDetailsTest) && execution(* *(..));
        
        after(TechnologyDetailsTest test) returning: within(TechnologyDetailsTest) && execution(* test*(..)) && this(test) {
        }
    }
}
