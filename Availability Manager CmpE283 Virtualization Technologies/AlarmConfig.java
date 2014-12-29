package cmpe283.project1;

import com.vmware.vim25.AlarmSetting;
import com.vmware.vim25.AlarmSpec;
import com.vmware.vim25.StateAlarmExpression;
import com.vmware.vim25.StateAlarmOperator;
import com.vmware.vim25.mo.Alarm;
import com.vmware.vim25.mo.AlarmManager;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;

public class AlarmConfig {

	public static Alarm createPowerAlarm(ServiceInstance si) throws Exception{

		ManagedEntity[] dcs = si.getRootFolder().getChildEntity();
		
		AlarmManager alarmMgr = si.getAlarmManager();
		AlarmSpec spec = new AlarmSpec();
		StateAlarmExpression expression = createStateAlarmExpression();
	
	    spec.setAction(null);
	    spec.setExpression(expression);
	    spec.setName("VM Power Off ALarm");
	    spec.setDescription("Monitor VM state and alarm when VM powers off");
	    spec.setEnabled(true);    
	    
	    AlarmSetting as = new AlarmSetting();
	    as.setReportingFrequency(0); //the alarm is allowed to trigger as often as possible
	    as.setToleranceRange(0);
	    
	    spec.setSetting(as);
	    Alarm[] alarms = alarmMgr.getAlarm(dcs[0]);
	    for (Alarm alarm : alarms) {
	    	if (alarm.getAlarmInfo().getName().equals(spec.getName()))
	    		alarm.removeAlarm();
	    }
	    Alarm alarm = alarmMgr.createAlarm(dcs[0], spec);
	    System.out.println("Alarm Power off state created!");
	    Thread.sleep(3000);
	    return alarm;
	}
	
	//Alarm is set to red when VM is powered off else green
	private static StateAlarmExpression createStateAlarmExpression() {
		StateAlarmExpression expression = new StateAlarmExpression();
		expression.setType("VirtualMachine");
		expression.setStatePath("runtime.powerState");
		expression.setOperator(StateAlarmOperator.isEqual);
		expression.setYellow("poweredOff");
		return expression;
	}
}
