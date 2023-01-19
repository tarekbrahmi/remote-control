import cv2

# Open the camera
cap = cv2.VideoCapture("/dev/bus/usb/001/007")

# Check if the camera is opened
if not cap.isOpened():
    print("Error opening camera")
    exit()

# Capture video frames
while True:
    ret, frame = cap.read()

    # Display the frames
    cv2.imshow("Camera", frame)

    # Exit if 'q' is pressed
    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

# Release the camera
cap.release()

# Close all windows
cv2.destroyAllWindows()
