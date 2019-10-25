<template>
    <div>
        <br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/>
        <b-button variant="outline-secondary" @click="modalShow = !modalShow">Add new record</b-button>
        <b-modal @submit="reloadFinances" title="New Record" ok-only v-model="modalShow"><NewRecord /></b-modal>
        <br/><br/>
        <b-table id="finance-table" striped hover :sort-desc.sync="sortDesc" :sort-by.sync="sortBy" :current-page="currentPage" :per-page="perPage" :items="finances" :fields="fields"></b-table>
        <b-pagination
            v-model="currentPage"
            :total-rows="rows"
            :per-page="perPage"
            align="center"
            aria-controls="finance-table">
        </b-pagination>
    </div>
</template>

<script>
    import NewRecord from './NewRecord.vue'

    export default {

        components: {
            NewRecord
        },

        methods: {
            reloadFinances() {
                fetch('http://localhost:8082/finances', {
                    method: 'get'
                }).then(response => {
                    return response.json()
                }).then(jsonData => {
                    this.finances = jsonData
                })
            }
        },

        data() {
            return {
                sortBy: 'date',
                sortDesc: true,
                fields: [
                    { key: 'name' },
                    { key: 'amount', 
                        formatter: (value, key, item) => {
                            return value != null ? value : 0
                        }
                    },
                    { key: 'recordType',
                        formatter: value => value === 'Income' ? '->' : '<-'
                    },
                    { key: 'date'},
                    { key: 'approved',
                        formatter: (value, key, item) => {
                            return value ? 'No' : 'Yes'
                        }
                    }
                ],
                finances: [],
                currentPage: 1,
                perPage: 5,
                modalShow: false
            }
        },

        computed: {
            rows() {
                return this.finances.length
            }
        },

        mounted: function() {
            this.reloadFinances();
        }
    }
</script>