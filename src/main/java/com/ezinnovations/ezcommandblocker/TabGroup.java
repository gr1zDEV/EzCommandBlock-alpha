package com.ezinnovations.ezcommandblocker;

import java.util.List;

public record TabGroup(int priority, String parent, List<String> commands) {
}
