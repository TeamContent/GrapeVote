package model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.bson.types.ObjectId;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import apps.appsProxy;
import database.db;
import esayhelper.DBHelper;
import esayhelper.formHelper;
import esayhelper.jGrapeFW_Message;
import esayhelper.formHelper.formdef;

public class voteModel {
	private static DBHelper vote;
	private static formHelper _form;
	static {
		vote = new DBHelper(appsProxy.configValue().get("db").toString(),
				"vote");
		_form = vote.getChecker();
	}

	private db bind() {
		return vote.bind(String.valueOf(appsProxy.appid()));
	}

	public voteModel() {
		_form.putRule("name", formdef.notNull);
		_form.putRule("vote", formdef.notNull);
	}

	public String AddVote(JSONObject object) {
		if (!_form.checkRule(object)) {
			return resultMessage(1, "");
		}
		String info = bind().data(object).insertOnce().toString();
		return find(info).toString();
	}

	@SuppressWarnings("unchecked")
	public int updateVote(String mid, JSONObject object) {
		if (object.containsKey("vote")) {
			object.put("vote", object.get("vote").toString());
		}
		return bind().eq("_id", new ObjectId(mid)).data(object).update() != null
				? 0 : 99;
	}

	public int deleteVote(String mid) {
		return bind().eq("_id", new ObjectId(mid)).delete() != null ? 0 : 99;
	}

	public int deleteVote(String[] mids) {
		bind().or();
		for (int i = 0; i < mids.length; i++) {
			bind().eq("_id", new ObjectId(mids[i]));
		}
		return bind().deleteAll() == mids.length ? 0 : 99;
	}

	public JSONArray find(JSONObject fileInfo) {
		for (Object object2 : fileInfo.keySet()) {
			bind().eq(object2.toString(), fileInfo.get(object2.toString()));
		}
		return bind().select();
	}

	public JSONObject find(String vid) {
		return bind().eq("_id", new ObjectId(vid)).find();
	}

	@SuppressWarnings("unchecked")
	public JSONObject page(int idx, int pageSize) {
		JSONArray array = bind().page(idx, pageSize);
		JSONObject object = new JSONObject();
		object.put("totalSize",
				(int) Math.ceil((double) bind().count() / pageSize));
		object.put("currentPage", idx);
		object.put("pageSize", pageSize);
		object.put("data", array);
		return object;
	}

	@SuppressWarnings("unchecked")
	public JSONObject page(int idx, int pageSize, JSONObject fileInfo) {
		for (Object object2 : fileInfo.keySet()) {
			bind().eq(object2.toString(), fileInfo.get(object2.toString()));
		}
		JSONArray array = bind().dirty().page(idx, pageSize);
		JSONObject object = new JSONObject();
		object.put("totalSize",
				(int) Math.ceil((double) bind().count() / pageSize));
		object.put("currentPage", idx);
		object.put("pageSize", pageSize);
		object.put("data", array);
		return object;
	}

	@SuppressWarnings("unchecked")
	public int votes(String vid, JSONObject object) {
		JSONObject objects = new JSONObject();
		JSONArray newarray = new JSONArray();
		// 获取当前投票
		JSONObject _obj = find(vid);
		String votes = _obj.get("vote").toString();
		JSONArray array = (JSONArray) JSONValue.parse(votes);
		for (int i = 0; i < array.size(); i++) {
			JSONObject object2 = (JSONObject) array.get(i);
			if (object2.get("itemid").toString().equals(object.get("itemid"))) {
				object2.put("count",
						Integer.parseInt(object2.get("count").toString()) + 1);
			}
			newarray.add(object2);
		}
		objects.put("vote", newarray.toString());
		return bind().eq("_id", new ObjectId(vid)).data(objects).update() != null
				? 0 : 99;
	}

	/**
	 * 将map添加至JSONObject中
	 * 
	 * @param map
	 * @param object
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public JSONObject AddMap(HashMap<String, Object> map, JSONObject object) {
		if (map.entrySet() != null) {
			Iterator<Entry<String, Object>> iterator = map.entrySet()
					.iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, Object> entry = (Map.Entry<String, Object>) iterator
						.next();
				if (!object.containsKey(entry.getKey())) {
					object.put(entry.getKey(), entry.getValue());
				}
			}
		}
		return object;
	}

	public String resultMessage(int num, String message) {
		String msg = "";
		switch (num) {
		case 0:
			msg = message;
			break;
		case 1:
			msg = "必填项没有填";
			break;
		default:
			msg = "其它异常";
			break;
		}
		return jGrapeFW_Message.netMSG(num, msg);
	}
}
