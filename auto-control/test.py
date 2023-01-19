import cv2
webcam = cv2.VideoCapture(0)

while True:
    (_, im) = webcam.read()
    cv2.imshow('OK cam', im)
    key = cv2.waitKey(0)
    if key == 27:
        break
