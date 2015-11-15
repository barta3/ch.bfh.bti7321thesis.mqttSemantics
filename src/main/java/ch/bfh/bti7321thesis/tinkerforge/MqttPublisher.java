package ch.bfh.bti7321thesis.tinkerforge;

import java.util.Map.Entry;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.tinkerforge.BrickletJoystick;
import com.tinkerforge.Device;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;

import ch.bfh.bti7321thesis.tinkerforge.devices.JoyStickDevice;
import ch.bfh.bti7321thesis.tinkerforge.devices.MqttThing;

public class MqttPublisher {
	
	private Logger LOG = Logger.getLogger(this.getClass().getName());
	
//	static final String broker = "tcp://iot.eclipse.org:1883";
//	static final String broker = "tcp://test.mosquitto.org:1883";
	private static final String BROKER = "tcp://46.101.165.125:1883";
	private static final String CLIENT_ID = "barta3Tinker";
	
	private MemoryPersistence persistence = new MemoryPersistence();
	private IMqttAsyncClient mqttClient;

	private boolean addRandomtoEvents = false;
	

	private MqttPublisher() {
		setUpMqtt();
	}
	
    private static class Holder {
    	private static MqttPublisher instance = new MqttPublisher();
    	
    }
	
	public static MqttPublisher getInstance() {
		return Holder.instance;
	}
	
	 
	
	private void setUpMqtt() {
		LOG.info("Connecting to broker: " + BROKER);
		try {
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setCleanSession(true);
			mqttClient = new MqttAsyncClient(BROKER, CLIENT_ID, persistence);
			
			mqttClient.connect(connOpts).waitForCompletion();
			mqttClient.setCallback(new MqttActionReveiver());
							   //"ch.bfh.barta3/X1-Carbon/tfstack1/Temperature IR Bricklet/qC1/actions/setAmbientTemperatureCallbackPeriod"
//			mqttClient.subscribe("ch.bfh.barta3/+/+/+/+/actions/#");
			mqttClient.subscribe("ch.bfh.barta3/+/+/+/+/actions/#", 2);
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LOG.info("MQTT Connected");

	}
	
	public void disconnect() {
		LOG.info("Disconnecting MQTT");
		try {
			mqttClient.disconnect();
//			mqttClient.disconnectForcibly();
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LOG.info("MQTT disconnected");
	}
	
	public void pubEvent(String stackHost, Device device, String eventName, Object payload) {
		
		String baseTopic = new BrickletToMqttConverter().getBaseTopic(device, stackHost) + "/events/" + eventName;
		
		String payloadStr = payload.toString();
		if(addRandomtoEvents ) {
			payloadStr += " " + Math.random();
		}
		
		pubEvent(baseTopic, payloadStr);
	}

//	public void publishTempIrState(String stackName, BrickletTemperatureIR brickletTemperatureIR) {
//
//		String baseTopic = new BrickletToMqttConverter().getBaseTopic(brickletTemperatureIR, stackName) + "/state";
//
//		try {
//			pubState(baseTopic + "/AmbientTemperatureCallbackPeriod", Long.toString(brickletTemperatureIR.getAmbientTemperatureCallbackPeriod()));
//			pubState(baseTopic + "/ObjectTemperatureCallbackPeriod", Long.toString(brickletTemperatureIR.getObjectTemperatureCallbackPeriod()));
//			pubState(baseTopic + "/DebouncePeriod", Long.toString(brickletTemperatureIR.getDebouncePeriod()));
//			pubState(baseTopic + "/Emissivity", Long.toString(brickletTemperatureIR.getEmissivity()));
//			pubState(baseTopic + "/AmbientTemperatureCallbackThreshold", brickletTemperatureIR.getAmbientTemperatureCallbackThreshold().toString());
//			pubState(baseTopic + "/ObjectTemperatureCallbackThreshold", brickletTemperatureIR.getObjectTemperatureCallbackThreshold().toString());
//		} catch (TimeoutException | NotConnectedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//	}
	
	public void publishDeviceState(MqttThing<? extends Device> thing) {
		String baseTopic = new BrickletToMqttConverter().getBaseTopic(thing) + "/state";
		LOG.info(thing.getState().entrySet().size() + "entries");
		for(Entry<String, Object> state : thing.getState().entrySet()) {
			LOG.info(state.getKey());
			pubState(baseTopic + "/" + state.getKey(), state.getValue());
		}
	}
	
	public void publishJoystickState(String stack, BrickletJoystick brickletJoystick) {
		String baseTopic = new BrickletToMqttConverter().getBaseTopic(brickletJoystick, stack) + "/state";
		try {
			pubState(baseTopic + "/AnalogValueCallbackPeriod", brickletJoystick.getAnalogValueCallbackPeriod());
			pubState(baseTopic + "/AnalogValueCallbackThreshold", brickletJoystick.getAnalogValueCallbackThreshold());
//			pubState(baseTopic + "/DebouncePeriod", brickletJoystick.getDebouncePeriod());
			pubState(baseTopic + "/PositionCallbackPeriod", brickletJoystick.getPositionCallbackPeriod());
			pubState(baseTopic + "/PositionCallbackThreshold", brickletJoystick.getPositionCallbackThreshold());
		} catch (TimeoutException | NotConnectedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		baseTopic = new BrickletToMqttConverter().getBaseTopic(brickletJoystick, stack) + "/actions";
		
		for(String action : new JoyStickDevice(brickletJoystick).getActions()) {
			pubAction(baseTopic + "/ " + action, null);
		}
	}
	
	private void pubEvent(String topic, String payload) {
		
		LOG.info("Publishing on " + topic + " data: " + payload);
		try {
			MqttMessage message = new MqttMessage(payload == null ? "".getBytes() : payload.getBytes());
			message.setQos(0);
			mqttClient.publish(topic, message);
			// TODO Auto-generated catch block
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	private void pubState(String topic, Object payload) {
		LOG.info("Publishing on " + topic + " data: " + payload);
		try {
			MqttMessage message = new MqttMessage(payload == null ? "".getBytes() : payload.toString().getBytes());
//			message.setRetained(true); // TODO: activate if stable
			mqttClient.publish(topic, message);
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void pubAction(String topic, Object payload) {
		LOG.info("Publishing on " + topic + " data: " + payload);
		try {
			MqttMessage message = new MqttMessage(payload == null ? "".getBytes() : payload.toString().getBytes());
//			message.setRetained(true); // TODO: activate if stable
			mqttClient.publish(topic, message).waitForCompletion();
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
