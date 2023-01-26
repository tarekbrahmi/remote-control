import cv2
import numpy as np
import utils


class LaneDetector:

    curveList = []
    avgVal = 10

    def getLaneCurve(self, img, display=2):

        imgCopy = img.copy()
        imgResult = img.copy()
        imgThres = utils.thresholding(img)

        hT, wT, c = img.shape
        points = utils.valTrackbars()
        imgWarp = utils.warpImg(imgThres, points, wT, hT)
        imgWarpPoints = utils.drawPoints(imgCopy, points)
        middlePoint, imgHist = utils.getHistogram(imgWarp, display=True, minPer=0.5, region=4)
        curveAveragePoint, imgHist = utils.getHistogram(imgWarp, display=True, minPer=0.9)
        curveRaw = curveAveragePoint - middlePoint
        self.curveList.append(curveRaw)
        if len(self.curveList) > self.avgVal:
            self.curveList.pop(0)
        curve = int(sum(self.curveList)/len(self.curveList))
        if display != 0:
            imgInvWarp = utils.warpImg(imgWarp, points, wT, hT, inv=True)
            imgInvWarp = cv2.cvtColor(imgInvWarp, cv2.COLOR_GRAY2BGR)
            imgInvWarp[0:hT // 3, 0:wT] = 0, 0, 0
            imgLaneColor = np.zeros_like(img)
            imgLaneColor[:] = 0, 255, 0
            imgLaneColor = cv2.bitwise_and(imgInvWarp, imgLaneColor)
            imgResult = cv2.addWeighted(imgResult, 1, imgLaneColor, 1, 0)
            midY = 450
            cv2.putText(imgResult, str(curve), (wT // 2 - 80, 85), cv2.FONT_HERSHEY_COMPLEX, 2, (255, 0, 255), 3)
            cv2.line(imgResult, (wT // 2, midY), (wT // 2 + (curve * 3), midY), (255, 0, 255), 5)
            cv2.line(imgResult, ((wT // 2 + (curve * 3)), midY - 25),
                     (wT // 2 + (curve * 3), midY + 25), (0, 255, 0), 5)
            for x in range(-30, 30):
                w = wT // 20
                cv2.line(imgResult, (w * x + int(curve // 50), midY - 10),
                         (w * x + int(curve // 50), midY + 10), (0, 0, 255), 2)
        if display == 2:
            imgStacked = utils.stackImages(0.7, ([img, imgWarpPoints, imgWarp],
                                                 [imgHist, imgLaneColor, imgResult]))
            cv2.imshow('ImageStack', imgStacked)
        elif display == 1:
            cv2.imshow('Resutlt', imgResult)
        curve = curve/100
        if curve > 1:
            curve == 1
        if curve < -1:
            curve == -1

        return curve
