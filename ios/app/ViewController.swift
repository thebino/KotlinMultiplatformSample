//
//  ViewController.swift
//  SampleProject
//
//  Created by Benjamin Stürmer on 02.03.19.
//  Copyright © 2019 Benjamin Stürmer. All rights reserved.
//

import UIKit
import SharedCode
import Mapbox
import CoreLocation

class ViewController: UIViewController, MGLMapViewDelegate {
    
    @IBOutlet
    private weak var mapView: MGLMapView!
    
    @IBOutlet
    private weak var label: UILabel!
    
    private weak var style : MGLStyle?
    
    private var features: [MGLShape & MGLFeature] = [MGLShape & MGLFeature]()
    
    private let locationManager = CLLocationManager()
    
    private var initialDeviceCoordinate: CLLocationCoordinate2D? {
        didSet {
            guard oldValue == nil,
                let location = initialDeviceCoordinate else { return }
            refresh(coordinate: location)
            mapView.setCenter(location,
                              zoomLevel: 10,
                              animated: true)
        }
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        mapView.delegate = self
        mapView.showsUserLocation = true
        mapView.setUserTrackingMode(.follow, animated: true)
        
        view.addSubview(mapView)
        
        locationManager.requestAlwaysAuthorization()
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        locationManager.delegate = self
    }
    
    func mapView(_ mapView: MGLMapView, didFinishLoading style: MGLStyle) {
        self.style = style
    }
    
    func updateMarkers() {
        guard let style = self.style else { return }
        
        let image = UIImage(named: "meetup")!.resizedImage(newSize: CGSize(width: 20, height: 20))
        style.setImage(image, forName: "meetup")
        
        let options: [MGLShapeSourceOption: Any] = [
            .clustered: true,
            .maximumZoomLevelForClustering: 15,
            .clusterRadius: 30
        ]
        
        let source: MGLShapeSource
        if let currentSource = style.source(withIdentifier: "source-id") as? MGLShapeSource {
            source = currentSource
        } else {
            source = MGLShapeSource(identifier: "source-id",
                                    features: features,
                                    options: options)
            style.addSource(source)
        }
        do {
            try style.removeSource(source, error: ())
            style.addSource(source)
        } catch {
            
        }
        
        
        var layer = self.mapView.style?.layer(withIdentifier: "layer-id") as? MGLSymbolStyleLayer
        if layer == nil {
            layer = MGLSymbolStyleLayer(identifier: "layer-id", source: source)
            layer!.iconImageName = NSExpression(forConstantValue: "meetup")
            layer!.iconIgnoresPlacement = NSExpression(forConstantValue: true)
            layer!.iconAllowsOverlap = NSExpression(forConstantValue: true)
            style.addLayer(layer!)
        }
    }
    
    func mapView(_ mapView1: MGLMapView, regionDidChangeWith reason: MGLCameraChangeReason, animated: Bool) {
        switch reason {
        case MGLCameraChangeReason.gesturePinch,
             MGLCameraChangeReason.gesturePan,
             MGLCameraChangeReason.gestureOneFingerZoom:
            
            // refresh with map center
            let latOff = mapView1.visibleCoordinateBounds.ne.latitude - mapView1.visibleCoordinateBounds.sw.latitude
            let lonOff = mapView1.visibleCoordinateBounds.ne.longitude - mapView1.visibleCoordinateBounds.sw.longitude
            let lat = mapView1.visibleCoordinateBounds.ne.latitude - latOff
            let lon = mapView1.visibleCoordinateBounds.ne.longitude - lonOff
            
            refresh(lat: lat, lon: lon)
            
        default:
            return
        }
    }
    
    private func refresh(coordinate: CLLocationCoordinate2D) {
        refresh(lat: coordinate.latitude, lon: coordinate.longitude)
    }
    
    private func refresh(lat: CLLocationDegrees, lon: CLLocationDegrees) {
        MeetupApi().upcomingEvents(latitude: lat, longitude: lon, success: { [weak self] response in
            var tmpFeatures = [MGLShape & MGLFeature]()
            
            for event in response.events {
                let venue = event.venue
                if venue != nil {
                    let feature = MGLPointFeature()
                    let lat = venue!.lat
                    let lon = venue!.lon
                    feature.coordinate = CLLocationCoordinate2D(latitude: lat, longitude: lon)
                    tmpFeatures.append(feature)
                }
            }
            self?.features = tmpFeatures
            
            self?.updateMarkers()
            self?.label.alpha = 0

            return KotlinUnit()
            }, error: { [weak self] error in
                self?.label.alpha = 255
                self?.label.text = "no events found! (\(error.message ?? "unknown"))"
                
                return KotlinUnit()
        })
    }
}

extension UIImage {
    func resizedImage(newSize: CGSize) -> UIImage {
        // Guard newSize is different
        guard self.size != newSize else { return self }
        
        UIGraphicsBeginImageContextWithOptions(newSize, false, 0.0)
        self.draw(in: CGRect(x: 0.0, y: 0.0, width: newSize.width, height: newSize.height))
        let newImage: UIImage = UIGraphicsGetImageFromCurrentImageContext()!
        UIGraphicsEndImageContext()
        return newImage
    }
}

extension ViewController: CLLocationManagerDelegate {
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard let location = locations.first else {
            return
        }
        self.initialDeviceCoordinate = location.coordinate
        locationManager.delegate = nil
    }
}
