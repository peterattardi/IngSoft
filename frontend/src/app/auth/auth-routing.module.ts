import { NgModule } from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {AuthComponent} from './auth.component';
import {ClientLoginComponent} from './client-login/client-login.component';
import {AdminLoginComponent} from './admin-login/admin-login.component';
import {RegistrationComponent} from './registration/registration.component';
import {ChooseMarketComponent} from './choose-market/choose-market.component';
import {SupermarketGuard} from './supermarket-guard';

const routes: Routes = [
  {
    path: '',
    component: AuthComponent,
    children: [
      { path: '', redirectTo: 'supermarket', pathMatch: 'full'},
      { path: 'login', component: ClientLoginComponent, canActivate: [SupermarketGuard] },
      { path: 'admin', component: AdminLoginComponent },
      { path: 'register', component: RegistrationComponent },
      { path: 'supermarket', component: ChooseMarketComponent }
    ]
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AuthRoutingModule {}
