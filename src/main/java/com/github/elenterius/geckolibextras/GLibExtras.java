package com.github.elenterius.geckolibextras;

import com.geckolib.constant.dataticket.DataTicket;

public final class GLibExtras {

	public static final String ID = "geckolib_extras";
	public static final DataTicket<Object> ITEM_HOST_TICKET = DataTicket.create(ID + ":item_host_object", Object.class);

	private GLibExtras() {}

}
