package com.futurice.android.reservator.model;

import java.util.ArrayList;
import java.util.Vector;

import android.util.Log;

import com.futurice.android.reservator.common.CacheMap;

public class CachedDataProxy extends DataProxy {
    // private static final long CACHE_ROOMS_FOR = 3600*1000; // 1 hour
    private static final long CACHE_RESERVATION_FOR = 60 * 1000; // 1 minute

    private final DataProxy dataProxy;
    private final CacheMap<String, ArrayList<Reservation>> reservationCache;
    private ArrayList<Room> rooms;

    public CachedDataProxy(DataProxy dataProxy) {
        this.dataProxy = dataProxy;
        this.reservationCache = new CacheMap<String, ArrayList<Reservation>>();
        this.rooms = null;
    }


    @Override
    public ArrayList<Reservation> getRoomReservations(Room r)
        throws ReservatorException {
        ArrayList<Reservation> reservations = reservationCache.get(r.getEmail());
        if (reservations == null) {
            Log.d("CACHE", "getRoomReservations -- " + r.getEmail());
            reservations = dataProxy.getRoomReservations(r);
       }
        reservationCache.put(r.getEmail(), reservations, CACHE_RESERVATION_FOR);
        return reservations;
    }

    @Override
    public ArrayList<Room> getRooms() {
        // TODO: do not cache forever
        if (this.rooms == null) {
            Log.d("CACHE", "getRooms");
            this.rooms = dataProxy.getRooms();
        }
        return this.rooms;
    }

    @Override
    public void reserve(Room room, TimeSpan timeSpan, String owner, String ownerEmail)
        throws ReservatorException {
        reservationCache.remove(room.getEmail());
        dataProxy.reserve(room, timeSpan, owner, ownerEmail);
    }

    @Override
    public void setCredentials(String user, String password) {
        dataProxy.setCredentials(user, password);
    }

    @Override
    public void setServer(String server) {
        dataProxy.setServer(server);
        clearCache();
    }

    /**
     * Force Refreshing the room, clearing the cached data for that room if it exists.
     *
     * @param room
     */
    public void forceRefreshRoomReservations(Room room) {
        this.reservationCache.remove(room.getEmail());
        this.refreshRoomReservations(room);
    }

    public void clearCache() {
        this.rooms = null;
        this.reservationCache.clear();
    }

    @Override
    public void cancelReservation(Reservation r) throws ReservatorException {
        dataProxy.cancelReservation(r);
    }

    @Override
    public void modifyReservationTimeSpan(Reservation r, Room room, TimeSpan timeSpan) throws ReservatorException {
        dataProxy.modifyReservationTimeSpan(r, room, timeSpan);
    }

}
