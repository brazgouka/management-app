<template>
    <div>
        <b-form @submit="onSubmit">
            <b-form-group class="form-grp" label-align="center" label="Countdown:" label-for="input-3">
                <b-form-select
                    v-model="countdown"
                    :options="countdowns"
                    required></b-form-select>
            </b-form-group>
            <b-button class="timer-btn" @click="startInterval" type="submit" variant="primary">Submit</b-button>
            <b-button class="timer-btn" @click="stopTimer" type="cancel" variant="danger">Stop</b-button>
        </b-form>
        <div class="d-inline-block block">
            <p class="digit">{{ minutes | two_digits }}</p>
            <p class="text">Minutes</p>
        </div>
        <div class="d-inline-block block">
            <p class="digit">{{ seconds | two_digits }}</p>
            <p class="text">Seconds</p>
        </div>
    </div>
</template>

<script>
export default {

    data() {
        return {
            pause: true,
            audio: new Audio('https://interactive-examples.mdn.mozilla.net/media/examples/t-rex-roar.mp3'),
            endTime: Number,
            countdown: '25',
            countdowns: ['1', '5', '15', '25', '55'],
            now: Math.trunc((new Date()).getTime() / 1000)
        }
    },

    computed: {
        seconds() {
            if(!this.pause) { 
                let seconds = Math.trunc((this.endTime - this.now)) % 60;
                if (seconds <= 0 && this.minutes <= 0) {
                    this.audio.play();
                }
                return  seconds > 0 ? seconds : 0;
            } else {
                return 0;
            }
        },

        minutes() {
            if(!this.pause) { 
                let minutes = Math.trunc((this.endTime - this.now) / 60) % 60;
                return minutes > 0 ? minutes : 0;
            } else {
                return 0;
            }
        }
    },

    methods: {
        startInterval: function () {
            this.pause = false; 
            const self = this;
            setInterval(function() {
                if(!self.pause) { 
                    self.now = Math.trunc((new Date()).getTime() / 1000);
                }
            }, 1000); 
        },
        stopTimer: function () {
            this.pause = true;
            this.audio.pause();
        },
        onSubmit(evt) {
            evt.preventDefault()
            this.endTime = this.now + parseInt(this.countdown) * 60;
        }
    }
}
</script>

<style scoped>
    @import url(https://fonts.googleapis.com/css?family=Roboto+Condensed:400|Roboto:100);

    .timer-btn {
        margin: 5px;
        margin-bottom: 0px;
    }

    .form-grp {
        width: 170px;
        margin: auto;
        margin-top: 100px;
        margin-bottom: 10px;
    }

    .block {
        flex-direction: column;
        margin: 30px;
        margin-top: 10px;
    }

    .text {
        color: rgb(139, 176, 255);
        font-size: 30px;
        font-family: 'Roboto Condensed', serif;
        font-weight: 40;
        margin-top:10px;
        margin-bottom: 10px;
    }

    .digit {
        color: #ecf0f1;
        font-size: 100px;
        font-weight: 100;
        font-family: 'Roboto', serif;
        margin: 10px;
    }
</style>