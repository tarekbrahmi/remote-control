from laneDetector import LaneDetector
from VideoStreamer import RealTimeVideoStreamer


def run_real_time_lane_detector():
    lane_detector = LaneDetector()
    RealTimeVideoStreamer(lane_detector=lane_detector).execute(
        applay_delay=False)


if __name__ == "__main__":
    run_real_time_lane_detector()
