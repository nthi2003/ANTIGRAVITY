package com.chitieu.domain.model;

public enum NetWorthRating {
    EXCELLENT, // Net Worth > 0 and growing > 20% YoY
    GOOD, // Net Worth > 0 and growing 5-20% YoY
    FAIR, // Net Worth > 0 but growing < 5% YoY
    POOR // Net Worth <= 0
}
