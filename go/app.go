package mobileproxy

import (
	"fmt"
	"log"
	"sync"
	"time"

	"golang.org/x/mobile/exp/app"
	"golang.org/x/mobile/exp/f32"
	"golang.org/x/mobile/exp/gl/glutil"
	"golang.org/x/mobile/gl"
)

var (
	images   *glutil.Images
	fps      *debug.FPS
	program  gl.Program
	position gl.Attrib
	color    gl.Uniform
)

func init() {
	app.Main(func(a app.App) {
		var glctx gl.Context
		for e := range a.Events() {
			switch e := a.Filter(e).(type) {
			case lifecycle.Event:
				switch e.Crosses(lifecycle.StageVisible) {
				case lifecycle.CrossOn:
					onStart(&glctx)
					a.Publish()
					a.Send(paint.Event{})
				case lifecycle.CrossOff:
					onStop()
					glctx = nil
				}
			case paint.Event:
				if glctx == nil || e.External {
					continue
				}
				onPaint(&glctx)
				a.Publish()
				a.Send(paint.Event{})
			}
		}
	})
}
