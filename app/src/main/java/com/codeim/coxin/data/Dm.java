package com.codeim.coxin.data;

import com.codeim.coxin.fanfou.DirectMessage;
import com.codeim.coxin.fanfou.User;
import com.codeim.coxin.util.TextHelper;

public class Dm extends Message {
	@SuppressWarnings("unused")
	private static final String TAG = "Dm";

	public boolean isSent;

	public static Dm create(DirectMessage directMessage, boolean isSent) {
		Dm dm = new Dm();

		dm.id = directMessage.getId();
		dm.text = directMessage.getText();
		dm.createdAt = directMessage.getCreatedAt();
		dm.isSent = isSent;

		User user = dm.isSent ? directMessage.getRecipient() : directMessage
				.getSender();
		dm.screenName = TextHelper.getSimpleTweetText(user.getScreenName());
		dm.userId = user.getId();
		dm.profileImageUrl = user.getProfileImageURL().toString();

		return dm;
	}
}