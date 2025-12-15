package fr.iandeveseleer.cicddashboardapi.processor;

import java.io.Serializable;

public interface CIEventProcessor {

    <T extends Serializable> void processEvent(T payload);
}
