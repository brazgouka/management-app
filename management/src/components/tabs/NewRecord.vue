<template>
    <div>
        <b-form @submit="onSubmit" @reset="onReset" v-if="show">
            <b-form-group
                id="input-group-1"
                label="Record Description:"
                label-for="input-1">
                <b-form-input
                    id="input-1"
                    v-model="form.name"
                    required
                    placeholder="Enter record description"></b-form-input>
            </b-form-group>

            <b-form-group id="input-group-2" label="Amount:" label-for="input-2">
                <b-form-input
                id="input-2"
                type="number"
                v-model="form.amount"
                required
                placeholder="Enter amount"></b-form-input>
            </b-form-group>

            <b-form-group id="input-group-3" label="Record Type:" label-for="input-3">
                <b-form-select
                id="input-3"
                v-model="form.recordType"
                :options="recordTypes"
                required></b-form-select>
            </b-form-group>

            <b-form-group id="input-group-4">
                <b-form-checkbox v-model="form.approved" value="false">Approved</b-form-checkbox>
            </b-form-group>

            <b-button class="mb-2 mr-sm-2 mb-sm-0" type="submit" variant="primary">Submit</b-button>
            <b-button type="reset" variant="danger">Reset</b-button>
        </b-form>
    </div>
</template>

<script>
  export default {
    data() {
      return {
        form: {
          name: '',
          approved: "false",
          amount: '',
          recordType: null
        },
        recordTypes: [{ text: 'Select One', value: null }, 'Income', 'Outcome'],
        show: true
      }
    },
    methods: {
      onSubmit(evt) {
        evt.preventDefault()

        fetch('http://localhost:8082/finances/save', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body:JSON.stringify(this.form)
        }).then(res => res.json())
            .then(data =>  console.log(data))
            .catch(err => console.log(err));
      },
      onReset(evt) {
        evt.preventDefault()
        
        this.form.name = ''
        this.form.amount = ''
        this.form.recordType = null
        this.form.approved = "false"
        
        this.show = false
        this.$nextTick(() => {
          this.show = true
        })
      }
    }
  }
</script>