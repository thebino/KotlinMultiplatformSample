package com.example.home

import androidx.lifecycle.*
import com.example.*
import com.example.util.SingleTimeEvent
import kotlin.random.Random


/**
 * In this case we extend {@link AndroidViewModel}
 * <p>
 * AndroidViewModel works identically like {@link ViewModel} but also offers access to the application context
 * wich we need to get a content resolver from to query contact informations from the phone.
 * <p>
 *
 */
class HomeViewModel : ViewModel(), LifecycleObserver {

    /**
     * wrap the SingleTimeEvent so it can't be accessed from outside.
     * SingleTimeEvent is triggered only once. Using a "normal" LiveData could result in misleading situations like
     * the state is not resetted or only one observer is notified.
     * https://medium.com/androiddevelopers/livedata-with-snackbar-navigation-and-other-upcomingEvents-the-singleliveevent-case-ac2622673150
     */
    private val _requestPermission = MutableLiveData<SingleTimeEvent<String>>()
    val requestPermission: LiveData<SingleTimeEvent<String>>
        get() = _requestPermission

    val locationPermissionGranted = MutableLiveData<Boolean>()

    val events: MutableLiveData<ArrayList<Event>> = MutableLiveData()


    fun refresh(latitude: Double, longitude: Double) {
        locationPermissionGranted.value?.let {
            if (it) {
                loadEvents(latitude, longitude)
            } else {
                events.postValue(arrayListOf())
            }
        }
    }


    fun requestPermission() {
        _requestPermission.value = SingleTimeEvent(Random.nextInt(0, 100).toString())
    }


    private fun loadEvents(latitude: Double, longitude: Double) {
        MeetupApi().upcomingEvents(latitude, longitude, object : (Response) -> Unit {
            override fun invoke(response: Response) {
                events.postValue(response.events as ArrayList<Event>?)
            }
        }, object : (Exception) -> Unit {
            override fun invoke(exception: Exception) {
                events.postValue(arrayListOf())
                warn("failed to load", exception)
            }
        })
    }
}
