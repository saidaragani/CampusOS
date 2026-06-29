package com.campusos.common_lib.event;

/**
 * Single source of truth for the RabbitMQ topology. Publishers send to
 * {@link #EXCHANGE} with one of the routing keys below; messaging-service binds
 * {@link #MESSAGING_QUEUE} to the exchange with {@code #} (all keys).
 */
public final class EventTopology {

    private EventTopology() {
    }

    /** Durable topic exchange all domain events flow through. */
    public static final String EXCHANGE = "campusos.events";

    /** Durable queue the messaging-service consumes (bound with "#"). */
    public static final String MESSAGING_QUEUE = "messaging.all";

    /** Binding pattern — messaging consumes every event. */
    public static final String ALL_KEYS = "#";

    // Routing keys
    public static final String ATTENDANCE_ABSENT = "attendance.marked.absent";
    public static final String LEAVE_REQUESTED = "leave.requested";
    public static final String LEAVE_DECIDED = "leave.decided";
    public static final String FEE_REMINDER = "fee.reminder";
    public static final String FEE_STATUS_CHANGED = "fee.status.changed";
    public static final String HOLIDAY_PUBLISHED = "holiday.published";
    public static final String ANNOUNCEMENT_PUBLISHED = "announcement.published";
    public static final String PASSWORD_RESET = "auth.password.reset";
}
